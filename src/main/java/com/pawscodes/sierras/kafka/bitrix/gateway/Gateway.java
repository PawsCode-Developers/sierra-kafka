package com.pawscodes.sierras.kafka.bitrix.gateway;

import com.pawscodes.sierras.kafka.bitrix.data.*;
import com.pawscodes.sierras.kafka.bitrix.data.entity.*;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.DiscountCliId;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.StockDataId;
import com.pawscodes.sierras.kafka.bitrix.exception.BitrixException;
import com.pawscodes.sierras.kafka.bitrix.exception.CustomException;
import com.pawscodes.sierras.kafka.bitrix.model.Customer;
import com.pawscodes.sierras.kafka.bitrix.model.bitrix.*;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.*;
import com.pawscodes.sierras.kafka.bitrix.util.BitrixUtils;
import com.pawscodes.sierras.kafka.bitrix.util.MigrationAppUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class Gateway {

    private static final long EVICTION_DELAY_MS = 60000;
    private static final Map<String, String> BLOCK = Map.of("0", "Activo", "1", "Inactivo", "2", "Bloqueado", "3", "No se puede usar");
    ConcurrentHashMap<Long, Long> timedMap = new ConcurrentHashMap<>();
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final BitrixUtils bitrixUtils;
    private final MigrationAppUtil migrationAppUtil;

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final AuditRepository auditRepository;
    private final QuotaRepository quotaRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final ConceptRepository conceptRepository;
    private final Concept2Repository concept2Repository;
    private final System1320Repository system1320Repository;
    private final PrdProcessRepository prdProcessRepository;
    private final CompanyDirRepository companyDirRepository;
    private final BillStatusRepository billStatusRepository;
    private final ConsecutiveRepository consecutiveRepository;
    private final DocumentPedRepository documentPedRepository;
    private final DiscountCliRepository discountCliRepository;
    private final FreightConfigRepository freightConfigRepository;
    private final DocumentLinPedRepository documentLinPedRepository;
    private final System1320HistoryRepository system1320HistoryRepository;
    private final SoftJSDocumentPedRepository softJSDocumentPedRepository;
    private final StockDependenciesRepository stockDependenciesRepository;
    private final DocumentPedHistoryRepository documentPedHistoryRepository;
    private final SoftJSDocumentLinPedRepository softJSDocumentLinPedRepository;
    private final DocumentLinPedHistoryRepository documentLinPedHistoryRepository;

    public Gateway(BitrixUtils bitrixUtils, MigrationAppUtil migrationAppUtil, UserRepository userRepository, StockRepository stockRepository, AuditRepository auditRepository, QuotaRepository quotaRepository, ProductRepository productRepository, CompanyRepository companyRepository, ConceptRepository conceptRepository, Concept2Repository concept2Repository, System1320Repository system1320Repository, PrdProcessRepository prdProcessRepository, CompanyDirRepository companyDirRepository, BillStatusRepository billStatusRepository, ConsecutiveRepository consecutiveRepository, DocumentPedRepository documentPedRepository, DiscountCliRepository discountCliRepository, FreightConfigRepository freightConfigRepository, DocumentLinPedRepository documentLinPedRepository, System1320HistoryRepository system1320HistoryRepository, SoftJSDocumentPedRepository softJSDocumentPedRepository, StockDependenciesRepository stockDependenciesRepository, DocumentPedHistoryRepository documentPedHistoryRepository, SoftJSDocumentLinPedRepository softJSDocumentLinPedRepository, DocumentLinPedHistoryRepository documentLinPedHistoryRepository) {
        this.bitrixUtils = bitrixUtils;
        this.migrationAppUtil = migrationAppUtil;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.auditRepository = auditRepository;
        this.quotaRepository = quotaRepository;
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.conceptRepository = conceptRepository;
        this.concept2Repository = concept2Repository;
        this.system1320Repository = system1320Repository;
        this.prdProcessRepository = prdProcessRepository;
        this.companyDirRepository = companyDirRepository;
        this.billStatusRepository = billStatusRepository;
        this.consecutiveRepository = consecutiveRepository;
        this.documentPedRepository = documentPedRepository;
        this.discountCliRepository = discountCliRepository;
        this.freightConfigRepository = freightConfigRepository;
        this.documentLinPedRepository = documentLinPedRepository;
        this.system1320HistoryRepository = system1320HistoryRepository;
        this.softJSDocumentPedRepository = softJSDocumentPedRepository;
        this.stockDependenciesRepository = stockDependenciesRepository;
        this.documentPedHistoryRepository = documentPedHistoryRepository;
        this.softJSDocumentLinPedRepository = softJSDocumentLinPedRepository;
        this.documentLinPedHistoryRepository = documentLinPedHistoryRepository;
    }

    private boolean filterFunnel(BitrixDeal deal) {
        return Arrays.stream(StageEnum.values())
                .anyMatch(stageEnum -> stageEnum.getValue().equalsIgnoreCase(deal.getStageId()));
    }

    private boolean filterProcess(BitrixDeal result) {
        return result.getStageId().equals(StageEnum.BANDEJA_DE_ENTRADA.getValue()) ||
                result.getStageId().equals(StageEnum.COTIZACION.getValue()) ||
                result.getStageId().equals(StageEnum.VALIDACION_COTIZACION.getValue()) ||
                result.getStageId().equals(StageEnum.SEGUIMIENTO_COTIZACION.getValue()) ||
                result.getStageId().equals(StageEnum.VALIDACION_PAGO_CUPO.getValue()) ||
                result.getStageId().equals(StageEnum.PEDIDO.getValue());
    }

    public void process(Long request) throws BitrixException {
        if (timedMap.get(request) == null) {
            timedMap.put(request, System.currentTimeMillis());
            BitrixResult<BitrixDeal> bitrixResult = getDeal(request);
            BitrixDeal deal = bitrixResult.getResult();

            if (filterFunnel(deal)) {
                log.info("Deal {}, start processing", request);
                String stageId = deal.getStageId();
                if (stageId.equals(StageEnum.CERRADO_PERDIDO.getValue()))
                    cancelOrder(deal);
                else if (filterProcess(deal) && validateOrder(deal)) {
                    BitrixResult<List<BitrixProductRows>> result = validatePrice(deal);
                    getOtherUnit(deal, result);
                    if (stageId.equals(StageEnum.BANDEJA_DE_ENTRADA.getValue()))
                        addClientInformation(deal);
                    else if (stageId.equals(StageEnum.COTIZACION.getValue()))
                        processQuote(deal, result);
                    else if (stageId.equals(StageEnum.PEDIDO.getValue()))
                        processOrder(deal, result);
                    else if (stageId.equals(StageEnum.SEGUIMIENTO_DEL_PEDIDO.getValue()))
                        validateOrderPrice(deal, result);
                }
                log.info("Deal {}, process finish at: {} secs", request, (double) (System.currentTimeMillis() - timedMap.get(request)) / 1000);
            }
            executorService.schedule(() -> timedMap.remove(request), 3, TimeUnit.SECONDS);
        }
    }

    private void addClientInformation(BitrixDeal deal) throws BitrixException {
        if (deal.getAddresses().isEmpty() && deal.getCompanyId() != 0) {
            BitrixCompany company = getCompany(deal.getCompanyId()).getResult();
            CompanyData companyData = companyRepository.findByNit(company.getNit());
            List<CompanyDirData> companyDirData = companyDirRepository.findByNit(company.getNit());

            BitrixResult<List<BitrixCustomFields>> customFields = bitrixUtils.getDealCustomFields(BitrixGetList.builder()
                            .filter(Map.of("FIELD_NAME", "UF_CRM_1750204659"))
                            .build())
                    .getBody();

            assert customFields != null;
            Map<String, Integer> clientStatus = customFields
                    .getResult()
                    .getFirst()
                    .getValues()
                    .stream()
                    .collect(Collectors.toMap(
                            BitrixCustomFields.ListItem::getValue,
                            BitrixCustomFields.ListItem::getId
                    ));

            deal.setClientState(String.valueOf(clientStatus.get(BLOCK.get(companyData.getBloqueo() != null ? companyData.getBloqueo() : 0))));
            deal.setAddresses("");

            if (companyData.getDireccion() != null) {
                deal.setAddresses("Dir: 0, " + companyData.getDireccion() + " | " +
                        companyData.getYPais().getDescripcion() + ", " +
                        companyData.getYDpto().getDescripcion() + ", " +
                        companyData.getYCiudad().getDescripcion() + "\n");
                deal.setDeliveryAddress("Dir: 0, " + companyData.getDireccion() + " | " +
                        companyData.getYPais().getDescripcion() + ", " +
                        companyData.getYDpto().getDescripcion() + ", " +
                        companyData.getYCiudad().getDescripcion() + "\n");
            }

            companyDirData.forEach(companyDirData1 -> {
                if (companyDirData1.getDir_activa() != null && companyDirData1.getDir_activa().equals("S"))
                    deal.setAddresses(deal.getAddresses() + "Dir: " + companyDirData1.getCodigoDireccion() + ", " + companyDirData1.getDireccion() + " | " +
                            companyDirData1.getYPais().getDescripcion() + ", " +
                            companyDirData1.getYDpto().getDescripcion() + ", " +
                            companyDirData1.getYCiudad().getDescripcion() + "\n");
            });
        }

        bitrixUtils.updateDeal(BitrixUpdate.builder()
                .id(String.valueOf(deal.getId()))
                .fields(deal)
                .build());
    }

    private void validateOrderPrice(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        AtomicBoolean hasToUpdateBitrixProducts = new AtomicBoolean(false);
        int noOrder = Integer.parseInt(deal.getNoOrder());
        DocumentPed ped = documentPedRepository.findByNumero(noOrder);
        DocumentPedHistory pedHistory = documentPedHistoryRepository.findByNumero(noOrder);
        SoftJSDocumentPed softJSDocumentPed = softJSDocumentPedRepository.findByNumero(noOrder);

        BitrixResult<List<BitrixUser>> userList = bitrixUtils.getUser(BitrixGetList.builder()
                        .filter(Map.of("ID", deal.getAssigned()))
                        .build())
                .getBody();
        UserData userData = userRepository.findByCorreointbitrix(userList.getResult().getFirst().getEmail());

        if (ped != null && userData != null && ped.getVendedor() != userData.getNit()) {
            ped.setVendedor(userData.getNit());
            documentPedRepository.save(ped);
            pedHistory.setVendedor(userData.getNit());
            documentPedHistoryRepository.save(pedHistory);
            softJSDocumentPed.setVendedor(userData.getNit());
            softJSDocumentPedRepository.save(softJSDocumentPed);
        }

        AtomicInteger count = new AtomicInteger();
        Map<String, Integer> itemOtherUnit = getStringIntegerMap(deal);
        result.getResult().forEach(productRows -> {
            count.getAndIncrement();
            DocumentLinPed linPed = documentLinPedRepository.findByNumeroAndCodigoAndSeq(noOrder, productRows.getProductName(), count.get());
            DocumentLinPedHistory linPedHistory = documentLinPedHistoryRepository.findByNumeroAndCodigoAndSeq(noOrder, productRows.getProductName(), count.get());
            SoftJSDocumentLinPed softJSDocumentLinPed = softJSDocumentLinPedRepository.findByNumeroAndCodigoAndSeq(noOrder, productRows.getProductName(), count.get());
            if (linPed != null) {
                if (linPed.getValorUnitario() != productRows.getPriceExclusive()) {
                    productRows.setPrice(linPed.getValorUnitario());
                    hasToUpdateBitrixProducts.set(true);
                }
                if (linPed.getCantidad() != productRows.getQuantity()) {
                    linPed.setCantidad(productRows.getQuantity());
                    linPed.setDespacho_virtual(linPed.getCantidad());
                    documentLinPedRepository.save(linPed);
                    linPedHistory.setCantidad(productRows.getQuantity());
                    linPedHistory.setDespacho_virtual(linPedHistory.getDespacho_virtual());
                    documentLinPedHistoryRepository.save(linPedHistory);
                    softJSDocumentLinPed.setCantidad(productRows.getQuantity());
                    softJSDocumentLinPedRepository.save(softJSDocumentLinPed);
                }

                Integer hasOtherUnit = itemOtherUnit.get(linPed.getCodigo() + "_" + linPed.getCantidad());
                if (hasOtherUnit != null && linPed.getCantidad_dos() != itemOtherUnit.get(linPed.getCodigo() + "_" + linPed.getCantidad())) {
                    linPed.setCantidad_dos(hasOtherUnit);
                    linPed.setCantidad_otra_und(productRows.getQuantity() / hasOtherUnit);
                    documentLinPedRepository.save(linPed);
                    linPedHistory.setCantidad_dos(hasOtherUnit);
                    linPedHistory.setCantidad_otra_und(linPed.getCantidad_otra_und());
                    documentLinPedHistoryRepository.save(linPedHistory);
                    softJSDocumentLinPed.setCantidaddos(hasOtherUnit);
                    softJSDocumentLinPed.setCantidadotraund(linPed.getCantidad_otra_und());
                    softJSDocumentLinPedRepository.save(softJSDocumentLinPed);
                }
            } else {
                Integer hasOtherUnit = itemOtherUnit.get(productRows.getProductName() + "_" + productRows.getQuantity());
                DocumentLinPed last = documentLinPedRepository.findLastByNumero(Integer.parseInt(deal.getNoOrder()));
                DocumentLinPed documentLinPed = documentLinPedRepository.save(DocumentLinPed.builder()
                        .seq(last.getSeq() + 1)
                        .codigo(productRows.getProductName())
                        .cantidad(productRows.getQuantity())
                        .valorUnitario(productRows.getPriceExclusive())
                        .numero(Integer.parseInt(deal.getNoOrder()))
                        .bodega(last.getBodega())
                        .porcentaje_iva(productRows.getTax())
                        .und(productRows.getMeasure())
                        .despacho_virtual(productRows.getQuantity())
                        .cantidad_dos(hasOtherUnit != null ? hasOtherUnit : 0)
                        .cantidad_otra_und(hasOtherUnit != null ? productRows.getQuantity() / hasOtherUnit : 0)
                        .build());

                documentLinPedHistoryRepository.save(DocumentLinPedHistory.builder()
                        .id(documentLinPed.getId())
                        .seq(last.getSeq() + 1)
                        .codigo(productRows.getProductName())
                        .cantidad(productRows.getQuantity())
                        .valorUnitario(productRows.getPriceExclusive())
                        .numero(Integer.parseInt(deal.getNoOrder()))
                        .bodega(last.getBodega())
                        .porcentaje_iva(productRows.getTax())
                        .und(productRows.getMeasure())
                        .despacho_virtual(productRows.getQuantity())
                        .cantidad_dos(hasOtherUnit != null ? hasOtherUnit : 0)
                        .cantidad_otra_und(hasOtherUnit != null ? productRows.getQuantity() / hasOtherUnit : 0)
                        .build());

                softJSDocumentLinPedRepository.save(SoftJSDocumentLinPed.builder()
                        .seq(last.getSeq() + 1)
                        .codigo(productRows.getProductName())
                        .cantidad(productRows.getQuantity())
                        .valorunitario(productRows.getPriceExclusive())
                        .numero(Integer.parseInt(deal.getNoOrder()))
                        .bodega(last.getBodega())
                        .und(productRows.getMeasure())
                        .porcentajeiva(productRows.getTax())
                        .cantidaddos(hasOtherUnit != null ? hasOtherUnit : 0)
                        .cantidadotraund(hasOtherUnit != null ? productRows.getQuantity() / hasOtherUnit : 0)
                        .build());
            }
        });

        if (documentLinPedRepository.findByNumero(noOrder).size() > result.getResult().size()) {
            documentLinPedRepository.deleteBySeqGreaterThanAndNumero(result.getResult().size(), noOrder);
            softJSDocumentLinPedRepository.deleteBySeqGreaterThanAndNumero(result.getResult().size(), noOrder);
            documentLinPedHistoryRepository.deleteBySeqGreaterThanAndNumero(result.getResult().size(), noOrder);
        }

        if (hasToUpdateBitrixProducts.get()) {
            updateProductDeal(BitrixUpdate.<BitrixProductRows>builder()
                    .id(String.valueOf(deal.getId()))
                    .rows(result.getResult())
                    .build());
        }
    }

    private boolean validateOrder(BitrixDeal result) {
        if (!result.getNoOrder().isEmpty() && (
                result.getStageId().equals(StageEnum.BANDEJA_DE_ENTRADA.getValue()) ||
                        result.getStageId().equals(StageEnum.COTIZACION.getValue()) ||
                        result.getStageId().equals(StageEnum.SEGUIMIENTO_COTIZACION.getValue()) ||
                        result.getStageId().equals(StageEnum.VALIDACION_PAGO_CUPO.getValue()) ||
                        result.getStageId().equals(StageEnum.PEDIDO.getValue())
        )) {
            log.info("Order is already exists: {}", result.getNoOrder());
            result.setStageId(StageEnum.SEGUIMIENTO_DEL_PEDIDO.getValue());
            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(result.getId()))
                    .fields(result)
                    .build());
            return false;
        }
        return true;
    }

    private BitrixResult<List<BitrixProductRows>> validatePrice(BitrixDeal deal) throws BitrixException {
        BitrixResult<List<BitrixProductRows>> result = getDeadProducts(deal.getId());
        StringBuilder comment = new StringBuilder("Productos con el precio incorrecto:\n");
        List<BitrixProductRows> bitrixProductRows = new ArrayList<>();
        boolean newToUpdate = false;
        boolean updateTax = false;

        if (deal.getStageId().equals(StageEnum.CERRADO_PERDIDO.getValue()))
            return result;
        else if (result != null && result.getResult().isEmpty()) {
            deal.setErrorMessage(getDateTime() + "\nNo tiene productos" + "\n\n" + deal.getErrorMessage());
            deal.setStageId(StageEnum.BANDEJA_DE_ENTRADA.getValue());
            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(deal.getId()))
                    .fields(deal)
                    .build());
            return result;
        } else {
            if (deal.getCompanyId() != 0) {
                deal.setDiscountDetails("");
                StringBuilder discountDetails = new StringBuilder();
                double tax = 0.0;
                for (BitrixProductRows productRows : result.getResult()) {
                    ProductData productData = productRepository.findById(productRows.getProductName())
                            .orElseGet(ProductData::new);
                    BitrixResult<BitrixCompany> company = getCompany(deal.getCompanyId());
                    CompanyData companyData = companyRepository.findByNit(company.getResult().getNit());

                    if (companyData.getEs_excento_iva() != null && productData.getManeja_inventario() == 1 && productRows.getTax() != 0) {
                        tax += (productRows.getBrutePrice() - productRows.getPriceNet()) * productRows.getQuantity();
                        productRows.setPrice(productRows.getPriceExclusive());
                        productRows.setBrutePrice(productRows.getPriceNet());
                        productRows.setTax(0);
                        newToUpdate = true;
                        updateTax = true;
                    }

                    Optional<DiscountCli> discountCli = discountCliRepository.findById(new DiscountCliId(company.getResult().getNit(), productData.getCodigo()));
                    if (discountCli.isPresent()) {
                        if (deal.getStageId().equals(StageEnum.BANDEJA_DE_ENTRADA.getValue()) ||
                                deal.getStageId().equals(StageEnum.COTIZACION.getValue()) ||
                                deal.getStageId().equals(StageEnum.PEDIDO.getValue())) {
                            double oldPrice = productData.getValor_unitario();
                            double discount = (double) Math.round(discountCli.get().getDescuento() * 100) / 100;
                            double newPrice = oldPrice * (1 - (discount / 100));
                            productRows.setPrice((double) Math.round((newPrice * (1 + ((double) productRows.getTax() / 100))) * (1 - (productRows.getDiscountRate() / 100)) * 100) / 100);
                            productRows.setPriceExclusive((double) Math.round(newPrice * (1 - (productRows.getDiscountRate() / 100)) * 100) / 100);
                            productRows.setPriceNet((double) Math.round(newPrice * 100) / 100);
                            productRows.setBrutePrice((double) Math.round(newPrice * (1 + ((double) productRows.getTax() / 100)) * 100) / 100);
                            discountDetails.append(deal.getDiscountDetails())
                                    .append("Ref: ")
                                    .append(productData.getCodigo())
                                    .append(" - Desc2: ")
                                    .append(discount)
                                    .append("% - Ant: ")
                                    .append(NumberFormat.getCurrencyInstance().format(oldPrice))
                                    .append(" - Nue: ")
                                    .append(NumberFormat.getCurrencyInstance().format(newPrice))
                                    .append("- DescBtx: ")
                                    .append(productRows.getDiscountRate())
                                    .append("%\n");
                            newToUpdate = true;
                        }
                        if (productRows.getPrice() < productData.getValor_unitario() * (1 - (discountCli.get().getDescuento() / 100))) {
                            comment.append("Producto: ").append(productRows.getProductName()).append(" tiene un mayor descuento\n");
                            newToUpdate = true;
                        }
                        if (productData.getPrecio_si_costo_cero() < productRows.getPrice() * (1 - ((discountCli.get().getDescuento() + productRows.getDiscountRate()) / 100))) {
                            comment.append("Producto: ").append(productRows.getProductName()).append(" tiene un menor precio del permitido\n");
                            newToUpdate = true;
                        }
                    } else if (productRows.getPrice() < productData.getPrecio_si_costo_cero()) {
                        comment.append("Producto: ").append(productRows.getProductName()).append(" tiene un menor precio del permitido\n");
                        newToUpdate = true;
                    }
                    bitrixProductRows.add(productRows);
                }
                if (updateTax)
                    deal.setAmount(deal.getAmount() - tax);
                deal.setDiscountDetails(discountDetails.toString());
            } else {
                comment = new StringBuilder("No hay compañia asignada");
                newToUpdate = true;
            }

            if (newToUpdate) {
                if ((comment.compareTo(new StringBuilder("Productos con el precio incorrecto:\n")) == 0)) {
                    comment = new StringBuilder();
                }

                if (!deal.getDiscountDetails().isEmpty() || updateTax) {
                    updateProductDeal(BitrixUpdate.<BitrixProductRows>builder()
                            .id(String.valueOf(deal.getId()))
                            .rows(bitrixProductRows)
                            .build());
                }

                if (!comment.isEmpty())
                    deal.setErrorMessage(getDateTime() + "\n" + comment + "\n\n" + deal.getErrorMessage());
                bitrixUtils.updateDeal(BitrixUpdate.builder()
                        .id(String.valueOf(deal.getId()))
                        .fields(deal)
                        .build());
            }
        }

        return result;
    }

    private boolean validateProductPrice(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result, CompanyData company) {
        boolean needAutorization = false;
        BitrixResult<List<BitrixUser>> userList = bitrixUtils.getUser(BitrixGetList.builder()
                        .filter(Map.of("ID", deal.getAssigned()))
                        .build())
                .getBody();

        assert userList != null;
        UserData userData = userRepository.findByCorreointbitrix(userList.getResult().getFirst().getEmail());

        BitrixResult<List<BitrixCustomFields>> customFields = bitrixUtils.getDealCustomFields(BitrixGetList.builder()
                        .filter(Map.of("FIELD_NAME", "UF_CRM_1743439978"))
                        .build())
                .getBody();

        assert customFields != null;
        Map<Integer, String> warehouseMap = customFields
                .getResult()
                .getFirst()
                .getValues()
                .stream()
                .collect(Collectors.toMap(
                        BitrixCustomFields.ListItem::getId,
                        BitrixCustomFields.ListItem::getValue
                ));

        int warehouse = Integer.parseInt(warehouseMap.get(Integer.parseInt(deal.getWarehouse())).split("-")[0].strip());
        for (BitrixProductRows productRows : result.getResult()) {
            List<System1320HistoryData> system1320HistoryData = system1320HistoryRepository
                    .findByNitAndProgramaStartingWithOrderByAutorizadoDesc(company.getNit(), "INTEGRACION BITRIX - " + deal.getId() + " - " + productRows.getProductName());

            boolean approved = system1320HistoryData.stream().noneMatch(s -> s.getAutorizado() == 1);

            List<System1320Data> system1320Data = system1320Repository.findByNitAndNotas(company.getNit(), String.valueOf(deal.getId()));
            if (!system1320Data.isEmpty())
                approved = false;

            if (approved) {
                ProductData productData = productRepository.findById(productRows.getProductName())
                        .orElseGet(ProductData::new);
                Optional<DiscountCli> discountCli = discountCliRepository.findById(new DiscountCliId(company.getNit(), productData.getCodigo()));
                if (discountCli.isPresent()) {
                    if (productRows.getDiscountRate() > 0 ||
                            (productRows.getPriceExclusive() < (productData.getPrecio_si_costo_cero() * (1 - (discountCli.get().getDescuento() / 100))))) {
                        system1320Repository.save(System1320Data.builder()
                                .item(productRows.getProductName())
                                .usuario(userData != null ? userData.getUsuario() : "BITRIX")
                                .condpagocliente(Integer.parseInt(company.getCondicion()))
                                .Valor_Documento(deal.getAmount())
                                .nit(company.getNit())
                                .condpagocliente(Integer.parseInt(company.getCondicion()))
                                .notas(String.valueOf(deal.getId()))
                                .mensaje("Precio no puede ser menor que el precio mínimo de venta.  Item:" +
                                        productRows.getProductName() +
                                        " Descripción:" + productData.getDescripcion() +
                                        " Precio mínimo: " + productData.getPrecio_si_costo_cero() +
                                        " Precio lista: " + productData.getValor_unitario() +
                                        " Valor venta:" + productRows.getPriceExclusive() +
                                        " Descuento parametrizado: " + discountCli.get().getDescuento() +
                                        " Descuento adicional: " + productRows.getDiscountRate()
                                )
                                .bodega(warehouse)
                                .tipo_autorizacion("F")
                                .build());
                        needAutorization = true;
                    }
                } else if (productRows.getPrice() < productData.getPrecio_si_costo_cero()) {
                    system1320Repository.save(System1320Data.builder()
                            .usuario(userData != null ? userData.getUsuario() : "BITRIX")
                            .item(productRows.getProductName())
                            .condpagocliente(Integer.parseInt(company.getCondicion()))
                            .Valor_Documento(deal.getAmount())
                            .nit(company.getNit())
                            .notas(String.valueOf(deal.getId()))
                            .mensaje("Precio no puede ser menor que el precio mínimo de venta.  Item:" +
                                    productRows.getProductName() +
                                    " Descripción:" + productData.getDescripcion() +
                                    " Precio mínimo: " + productData.getPrecio_si_costo_cero() +
                                    " Precio lista: " + productData.getValor_unitario() +
                                    " Valor venta:" + productRows.getPriceExclusive()
                            )
                            .bodega(warehouse)
                            .tipo_autorizacion("F")
                            .build());
                    needAutorization = true;
                }
            }
        }
        return needAutorization;
    }

    private void processQuote(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        if (deal.getCompanyId() != 0) {
            BitrixResult<BitrixCompany> company = getCompany(deal.getCompanyId());
            CompanyData companyData = companyRepository.findByNit(company.getResult().getNit());
            if (validateProductPrice(deal, result, companyData)) {
                deal.setStageId(StageEnum.VALIDACION_COTIZACION.getValue());
            } else if (result.getResult().stream().noneMatch(bitrixProductRows -> bitrixProductRows.getProductName().contains("FLETES"))) {
                String stage = StageEnum.SEGUIMIENTO_COTIZACION.getValue();
                String comments = "";
                double price = 0;
                if (result.getResult().isEmpty()) {
                    stage = StageEnum.BANDEJA_DE_ENTRADA.getValue();
                    comments = "No se encontro ningun producto";
                } else {
                    boolean isChemist = false;
                    for (BitrixProductRows productRows : result.getResult()) {
                        ProductData productData = productRepository.findByCodigo(productRows.getProductName());
                        if (productData != null && productData.getGrupo().getDescripcion().equals("QUIMICOS"))
                            isChemist = true;
                        price += productRows.getPrice() * productRows.getQuantity();
                    }

                    BitrixResult<List<BitrixCustomFields>> customFields = bitrixUtils.getDealCustomFields(BitrixGetList.builder()
                                    .filter(Map.of("FIELD_NAME", "UF_CRM_1744648899"))
                                    .build())
                            .getBody();

                    assert customFields != null;
                    Map<Integer, String> freightTypes = customFields.getResult()
                            .getFirst()
                            .getValues()
                            .stream()
                            .collect(Collectors.toMap(BitrixCustomFields.ListItem::getId, BitrixCustomFields.ListItem::getValue));

                    int deliveryCode = 0;
                    if (!deal.getDeliveryAddress().isEmpty() && !deal.getDeliveryAddress().startsWith("Dir:")) {
                        deliveryCode = -1;
                    } else if (deal.getDeliveryAddress().startsWith("Dir:")) {
                        String s = deal.getDeliveryAddress().strip().split(",")[0].strip().split(":")[1].replace(" ", "");
                        deliveryCode = Integer.parseInt(s);
                    }
                    CompanyDirData companyDirData = companyDirRepository.findByNitAndCodigoDireccion(companyData.getNit(), deliveryCode);
                    FreightConfigData freightConfigData;
                    if (companyDirData != null) {
                        freightConfigData = freightConfigRepository.findByTipofleteAndCategoriaclienteAndPaisAndDepartamentoAndCiudad(
                                freightTypes.get(deal.getFreightType()).substring(0, 1),
                                companyData.getConcepto_14(),
                                companyDirData.getYPais().getPais(),
                                companyDirData.getYDpto().getDepartamento(),
                                companyDirData.getYCiudad().getCiudad());
                    } else if (deliveryCode < 0) {
                        freightConfigData = null;
                    } else {
                        freightConfigData = freightConfigRepository.findByTipofleteAndCategoriaclienteAndPaisAndDepartamentoAndCiudad(
                                freightTypes.get(deal.getFreightType()).substring(0, 1),
                                companyData.getConcepto_14(),
                                companyData.getYPais() != null ? companyData.getYPais().getPais() : "",
                                companyData.getYDpto() != null ? companyData.getYDpto().getDepartamento() : "",
                                companyData.getYCiudad() != null ? companyData.getYCiudad().getCiudad() : "");
                    }

                    if (freightConfigData != null && freightConfigData.getValorminimoventa() > price) {
                        Optional<ProductData> productData = isChemist ? productRepository.findByFreight().stream()
                                .filter(p -> p.getValor_unitario() == freightConfigData.getValorflete())
                                .findFirst() : productRepository.findByFreight().stream()
                                .filter(p -> p.getValor_unitario() == freightConfigData.getValorflete())
                                .skip(1)
                                .findFirst();

                        if (productData.isPresent()) {
                            BitrixResult<Map<String, List<BitrixGetProduct>>> mapBitrixResult = bitrixUtils.getProductByFilter(BitrixGetList.builder()
                                            .select(List.of("id", "iblockId"))
                                            .filter(Map.of("iblockId", 14, "code", productData.get().getCodigo()))
                                            .build())
                                    .getBody();

                            assert mapBitrixResult != null;
                            result.getResult().add(BitrixProductRows.builder()
                                    .productId(mapBitrixResult.getResult().get("products").getFirst().getId())
                                    .productName(productData.get().getCodigo())
                                    .price(productData.get().getValor_unitario())
                                    .quantity(1)
                                    .build());

                            updateProductDeal(BitrixUpdate.<BitrixProductRows>builder()
                                    .id(String.valueOf(deal.getId()))
                                    .rows(result.getResult())
                                    .build());
                        }
                    }
                }

                deal.setStageId(stage);
                if (!comments.isEmpty())
                    deal.setErrorMessage(getDateTime() + "\n" + comments + "\n\n" + deal.getErrorMessage());
            } else {
                deal.setStageId(StageEnum.SEGUIMIENTO_COTIZACION.getValue());
            }
        } else {
            deal.setStageId(StageEnum.BANDEJA_DE_ENTRADA.getValue());
            deal.setErrorMessage(getDateTime() + "\nNo se encontro ninguna compañia\n\n" + deal.getErrorMessage());
        }
        bitrixUtils.updateDeal(BitrixUpdate.builder()
                .id(String.valueOf(deal.getId()))
                .fields(deal)
                .build());
    }

    private boolean validateQuota(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        BitrixResult<BitrixCompany> company = getCompany(deal.getCompanyId());
        List<System1320HistoryData> system1320HistoryData = system1320HistoryRepository
                .findByNitAndProgramaOrderByAutorizadoDesc(company.getResult().getNit(), "INTEGRACION BITRIX - " + deal.getId() + " - CLIENTE");

        boolean approved = system1320HistoryData.stream().noneMatch(s -> s.getAutorizado() == 1);

        List<System1320Data> system1320Data = system1320Repository.findByNitAndNotas(company.getResult().getNit(), String.valueOf(deal.getId()));
        if (!system1320Data.isEmpty()) {
            deal.setStageId(StageEnum.VALIDACION_PAGO_CUPO.getValue());
            return false;
        }

        if (approved) {
            CompanyData companyData = companyRepository.findByNit(company.getResult().getNit());
            double price = 0;
            for (BitrixProductRows productRows : result.getResult()) {
                price += productRows.getPrice() * productRows.getQuantity();
            }

            List<QuotaData> quotaDataList = quotaRepository.findAllByNit(companyData.getNit());

            AtomicReference<Double> total = new AtomicReference<>((double) 0);
            AtomicReference<Double> totalPending = new AtomicReference<>((double) 0);
            AtomicReference<Double> totalPassDue = new AtomicReference<>((double) 0);
            quotaDataList.forEach(quotaData -> {
                total.updateAndGet(v -> v + quotaData.getSaldo());
                totalPending.updateAndGet(v -> v + quotaData.getSin_Vencer());
                totalPassDue.updateAndGet(v -> v + quotaData.getVencida());
            });

            boolean needUpdate = false;
            String message = "";
            if (companyData.getCondicion().equals("0") || companyData.getCondicion().equals("00")) {
                message = "El Cliente paga de contado";
                needUpdate = true;
            } else if (totalPassDue.get() > 0) {
                message = "El Cliente está en mora por valor de: " + new DecimalFormat("#.################").format(totalPassDue.get());
                needUpdate = true;
            } else if (totalPending.get() > 0 && total.get() + price > companyData.getCupo_credito()) {
                message = "Cupo de crédito es insuficiente, Cupo disponible: " + (companyData.getCupo_credito() - totalPending.get());
                needUpdate = true;
            }

            if (needUpdate) {
                BitrixResult<List<BitrixUser>> userList = bitrixUtils.getUser(BitrixGetList.builder()
                                .filter(Map.of("ID", deal.getAssigned()))
                                .build())
                        .getBody();

                assert userList != null;
                UserData userData = userRepository.findByCorreointbitrix(userList.getResult().getFirst().getEmail());

                BitrixResult<List<BitrixCustomFields>> customFields = bitrixUtils.getDealCustomFields(BitrixGetList.builder()
                                .filter(Map.of("FIELD_NAME", "UF_CRM_1743439978"))
                                .build())
                        .getBody();

                assert customFields != null;
                Map<Integer, String> warehouseMap = customFields
                        .getResult()
                        .getFirst()
                        .getValues()
                        .stream()
                        .collect(Collectors.toMap(
                                BitrixCustomFields.ListItem::getId,
                                BitrixCustomFields.ListItem::getValue
                        ));

                int warehouse = Integer.parseInt(warehouseMap.get(Integer.parseInt(deal.getWarehouse())).split("-")[0].strip());

                system1320Repository.save(System1320Data.builder()
                        .usuario(userData != null ? userData.getUsuario() : "BITRIX")
                        .condpagocliente(Integer.parseInt(companyData.getCondicion()))
                        .Valor_Documento(deal.getAmount())
                        .nit(company.getResult().getNit())
                        .condpagocliente(Integer.parseInt(companyData.getCondicion()))
                        .condpagodocumento(Integer.parseInt(companyData.getCondicion()))
                        .notas(String.valueOf(deal.getId()))
                        .mensaje(message)
                        .bodega(warehouse)
                        .item("CLIENTE")
                        .tipo_autorizacion("C")
                        .build());

                deal.setErrorMessage(getDateTime() + "\n" + message + "\n\n" + deal.getErrorMessage());
                deal.setStageId(StageEnum.VALIDACION_PAGO_CUPO.getValue());
                bitrixUtils.updateDeal(BitrixUpdate.builder()
                        .id(String.valueOf(deal.getId()))
                        .fields(deal)
                        .build());
                return false;
            }
            return price != 0;
        }
        return true;
    }

    @Transactional
    public void processOrder(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        if (deal.getNoOrder().isEmpty() && deal.getCompanyId() != 0) {
            BitrixResult<BitrixCompany> company = getCompany(deal.getCompanyId());
            CompanyData companyData = companyRepository.findByNit(company.getResult().getNit());

            if (companyData == null || companyData.getNit() == 0) {
                deal.setStageId(StageEnum.BANDEJA_DE_ENTRADA.getValue());
                deal.setErrorMessage(getDateTime() + "\nLa compañia no existe en DMS" + "\n\n" + deal.getErrorMessage());
            } else if (result.getResult().isEmpty()) {
                deal.setStageId(StageEnum.BANDEJA_DE_ENTRADA.getValue());
            } else if (validateProductPrice(deal, result, companyData)) {
                deal.setStageId(StageEnum.VALIDACION_PAGO_CUPO.getValue());
            } else if (validateQuota(deal, result)) {
                BitrixResult<List<BitrixCustomFields>> customFields = bitrixUtils.getDealCustomFields(BitrixGetList.builder()
                                .build())
                        .getBody();

                assert customFields != null;
                Map<String, Map<Integer, String>> fieldsValues = customFields.getResult().stream()
                        .filter(bitrixCustomFields -> bitrixCustomFields.getFieldName().matches("UF_CRM_1743439978|UF_CRM_1743774849|UF_CRM_1743774680|UF_CRM_1735241718870"))
                        .collect(Collectors.toMap(
                                BitrixCustomFields::getFieldName,
                                listItem -> listItem.getValues().stream().collect(Collectors.toMap(
                                                BitrixCustomFields.ListItem::getId,
                                                BitrixCustomFields.ListItem::getValue
                                        )
                                )));

                Map<Integer, String> warehouseMap = fieldsValues.get("UF_CRM_1743439978");
                Map<Integer, String> conceptMap = fieldsValues.get("UF_CRM_1743774849");
                Map<Integer, String> concept2Map = fieldsValues.get("UF_CRM_1743774680");
                Map<Integer, String> validDayMap = fieldsValues.get("UF_CRM_1735241718870");

                BitrixResult<List<BitrixUser>> userList = bitrixUtils.getUser(BitrixGetList.builder()
                                .filter(Map.of("ID", deal.getAssigned()))
                                .build())
                        .getBody();
                assert userList != null;
                UserData userData = userRepository.findByCorreointbitrix(userList.getResult().getFirst().getEmail());

                String warehouse = warehouseMap.get(Integer.parseInt(deal.getWarehouse())).split("-")[0].strip();
                int concept = 0;
                Optional<ConceptData> conceptData = conceptRepository.findById(conceptMap.get(deal.getConcept()));
                if (conceptData.isPresent())
                    concept = conceptData.get().getConcepto();

                int concept2 = 0;
                Optional<Concept2Data> concept2Data = concept2Repository.findById(concept2Map.get(deal.getConcept2()));
                if (concept2Data.isPresent())
                    concept2 = concept2Data.get().getConcepto();

                String stage = StageEnum.SEGUIMIENTO_DEL_PEDIDO.getValue();
                AtomicReference<String> comment = new AtomicReference<>("Productos sin existencia:\n");
                LocalDate date = LocalDate.now();
                AtomicInteger seq = new AtomicInteger(1);

                int deliveryCode = 0;
                if (deal.getDeliveryAddress().startsWith("Dir:")) {
                    String s = deal.getDeliveryAddress().strip().split(",")[0].strip().split(":")[1].replace(" ", "");
                    deliveryCode = Integer.parseInt(s);
                }

                Optional<ConsecutiveData> consecutiveDataOptional = consecutiveRepository.findById(getDocumentNumber(warehouse));
                try {
                    if (consecutiveDataOptional.isPresent()) {
                        if (userData == null)
                            deal.setErrorMessage(getDateTime() + "\nNo existe el vendedor en DMS" + "\n\n" + deal.getErrorMessage());

                        DocumentPed documentPed = documentPedRepository.save(DocumentPed.builder()
                                .numero(consecutiveDataOptional.get().getNext())
                                .bodega(Integer.parseInt(warehouse))
                                .nit(companyData.getNit())
                                .valor_total(deal.getAmount())
                                .fecha(date)
                                .fecha_hora(LocalDateTime.now())
                                .vendedor(userData != null ? userData.getNit() : 0)
                                .diasValidez(Integer.parseInt(validDayMap.get(Integer.parseInt(deal.getValidDay())).split(" ")[0]))
                                .condicion(companyData.getCondicion())
                                .notas(removeBBCode(deal.getComments().strip()))
                                .usuario(userData != null ? userData.getUsuario() : "BITRIX")
                                .Nit_Usuario(String.valueOf(userData != null ? userData.getNit() : 0))
                                .concepto(concept)
                                .concepto2(concept2)
                                .documento(consecutiveDataOptional.get().getNext() + "-" + deal.getId())
                                .codigo_direccion(deliveryCode)
                                .build());

                        SoftJSDocumentPed softJSDocumentPed = softJSDocumentPedRepository.save(SoftJSDocumentPed.builder()
                                .idDocPed(documentPed.getId())
                                .fecha(documentPed.getFecha())
                                .fechaHora(documentPed.getFecha_hora())
                                .bodega(documentPed.getBodega())
                                .valorTotal(documentPed.getValor_total())
                                .nit(documentPed.getNit())
                                .numero(documentPed.getNumero())
                                .vendedor(documentPed.getVendedor())
                                .diasvalidez(documentPed.getDiasValidez())
                                .condicion(companyData.getCondicion())
                                .notas(removeBBCode(deal.getComments()))
                                .usuario(userData != null ? userData.getUsuario() : "BITRIX")
                                .usuarioactualizo(userData != null ? userData.getUsuario() : "BITRIX")
                                .fechahoraactualizo(documentPed.getFecha_hora())
                                .concepto(documentPed.getConcepto())
                                .concepto2(documentPed.getConcepto2())
                                .documento(documentPed.getDocumento())
                                .codigodireccion(documentPed.getCodigo_direccion())
                                .build());

                        documentPedHistoryRepository.save(DocumentPedHistory.builder()
                                .pc(documentPed.getPc())
                                .codigo_direccion(documentPed.getCodigo_direccion())
                                .anulado(documentPed.getAnulado())
                                .fletes(documentPed.getFletes())
                                .numero(documentPed.getNumero())
                                .bodega(documentPed.getBodega())
                                .nit(documentPed.getNit())
                                .valor_total(documentPed.getValor_total())
                                .fecha(documentPed.getFecha())
                                .fecha_hora(documentPed.getFecha_hora())
                                .vendedor(documentPed.getVendedor())
                                .diasValidez(documentPed.getDiasValidez())
                                .condicion(documentPed.getCondicion())
                                .notas(documentPed.getNotas())
                                .usuario(documentPed.getUsuario())
                                .concepto2(documentPed.getConcepto2())
                                .documento(documentPed.getDocumento())
                                .codigo_direccion(documentPed.getCodigo_direccion())
                                .build());

                        Map<String, Integer> itemOtherUnit = getStringIntegerMap(deal);

                        result.getResult().forEach(bitrixProductRows -> {
                            boolean hasStock = true;
                            ProductData productData = productRepository.findByCodigo(bitrixProductRows.getProductName());

                            if (productData.getManeja_inventario() == 1) {
                                Optional<StockData> stockDataList = stockRepository.findById(new StockDataId(
                                        bitrixProductRows.getProductName(),
                                        warehouse,
                                        date.getYear(),
                                        date.getMonthValue()
                                ));

                                if (stockDataList.isPresent()) {
                                    AtomicReference<Double> realStock = new AtomicReference<>(stockDataList.get().getStock());
                                    stockDependenciesRepository.findById(stockDataList.get().getCodigo())
                                            .ifPresent(stockDependenciesData -> stockRepository.findById(new StockDataId(
                                                            stockDependenciesData.getCodigodepende(),
                                                            warehouse,
                                                            date.getYear(),
                                                            date.getMonthValue()
                                                    ))
                                                    .ifPresent(stockData -> realStock
                                                            .updateAndGet(v -> v + (stockData.getStock() * stockDependenciesData.getCantidad()))));
                                    if (realStock.get() < bitrixProductRows.getQuantity())
                                        hasStock = false;
                                }
                            }

                            if (!hasStock) {
                                comment.getAndUpdate(s -> s + bitrixProductRows.getProductName() + "\n\n");
                                throw new CustomException("No tiene stock disponible");
                            } else {
                                Integer hasOtherUnit = itemOtherUnit.get(bitrixProductRows.getProductName() + "_" + bitrixProductRows.getQuantity());

                                DocumentLinPed documentLinPed = documentLinPedRepository.save(DocumentLinPed.builder()
                                        .seq(seq.getAndAdd(1))
                                        .codigo(bitrixProductRows.getProductName())
                                        .cantidad(bitrixProductRows.getQuantity())
                                        .valorUnitario(bitrixProductRows.getPriceExclusive())
                                        .numero(consecutiveDataOptional.get().getNext())
                                        .bodega(Integer.parseInt(warehouse))
                                        .porcentaje_iva(bitrixProductRows.getTax())
                                        .und(bitrixProductRows.getMeasure())
                                        .despacho_virtual(bitrixProductRows.getQuantity())
                                        .cantidad_otra_und(hasOtherUnit != null && hasOtherUnit > 0 ? bitrixProductRows.getQuantity() / hasOtherUnit : 0)
                                        .cantidad_dos(hasOtherUnit != null ? hasOtherUnit : 0)
                                        .build());

                                softJSDocumentLinPedRepository.save(SoftJSDocumentLinPed.builder()
                                        .seq(documentLinPed.getSeq())
                                        .bodega(documentLinPed.getBodega())
                                        .iddoclinped(documentLinPed.getId())
                                        .cantidad(documentLinPed.getCantidad())
                                        .numero(documentLinPed.getNumero())
                                        .valorunitario(documentLinPed.getValorUnitario())
                                        .codigo(bitrixProductRows.getProductName())
                                        .porcentajeiva(documentLinPed.getPorcentaje_iva())
                                        .und(documentLinPed.getUnd())
                                        .idsdp(softJSDocumentPed.getId())
                                        .cantidadotraund(documentLinPed.getCantidad_otra_und())
                                        .cantidaddos(documentLinPed.getCantidad_dos())
                                        .notas("")
                                        .build());

                                DocumentLinPedHistory pedHistory = documentLinPedHistoryRepository.save(DocumentLinPedHistory.builder()
                                        .id(documentLinPed.getId())
                                        .seq(documentLinPed.getSeq())
                                        .codigo(documentLinPed.getCodigo())
                                        .cantidad(documentLinPed.getCantidad())
                                        .valorUnitario(documentLinPed.getValorUnitario())
                                        .numero(documentLinPed.getNumero())
                                        .bodega(documentLinPed.getBodega())
                                        .porcentaje_iva(documentLinPed.getPorcentaje_iva())
                                        .und(documentLinPed.getUnd())
                                        .despacho_virtual(documentLinPed.getDespacho_virtual())
                                        .cantidad_und(documentLinPed.getCantidad_und())
                                        .cantidad_dos(documentLinPed.getCantidad_dos())
                                        .cantidad_otra_und(documentLinPed.getCantidad_otra_und())
                                        .build());
                                log.debug("Last record: {}", pedHistory.getId());
                            }
                        });

                        deal.setErrorMessage("");
                        deal.setNoOrder(String.valueOf(documentPed.getNumero()));

                        consecutiveRepository.save(ConsecutiveData.builder()
                                .type(consecutiveDataOptional.get().getType())
                                .next(consecutiveDataOptional.get().getNext() + 1)
                                .build());

                        auditRepository.save(AuditData.builder()
                                .que("Creó Pedido " + deal.getNoOrder())
                                .usuario(userData != null ? userData.getUsuario() : "BITRIX")
                                .build());
                    }
                } catch (Exception e) {
                    DocumentPed documentPed = documentPedRepository.findByNumero(consecutiveDataOptional.get().getNext());
                    if (documentPed != null) {
                        documentPedRepository.delete(documentPed);

                        SoftJSDocumentPed softJSDocumentPed = softJSDocumentPedRepository.findByNumero(documentPed.getNumero());
                        softJSDocumentPedRepository.delete(softJSDocumentPed);

                        DocumentPedHistory documentPedHistory = documentPedHistoryRepository.findByNumero(documentPed.getNumero());
                        documentPedHistoryRepository.delete(documentPedHistory);

                        List<DocumentLinPed> documentLinPedList = documentLinPedRepository.findByNumero(documentPed.getNumero());
                        documentLinPedRepository.deleteAll(documentLinPedList);

                        List<SoftJSDocumentLinPed> softJSDocumentLinPeds = softJSDocumentLinPedRepository.findByNumero(documentPed.getNumero());
                        softJSDocumentLinPedRepository.deleteAll(softJSDocumentLinPeds);

                        List<DocumentLinPedHistory> documentLinPedHistories = documentLinPedHistoryRepository.findByNumero(documentPed.getNumero());
                        documentLinPedHistoryRepository.deleteAll(documentLinPedHistories);
                    }

                    deal.setErrorMessage(getDateTime() + "\nOcurrio un error, redirijase al area de soporte, " + e.getMessage() + "\n\n" + deal.getErrorMessage());
                    log.error(e.getMessage());

                    stage = StageEnum.SEGUIMIENTO_COTIZACION.getValue();
                }

                if (comment.compareAndSet("Productos sin existencia:\n", ""))
                    deal.setStageId(stage);
                else {
                    deal.setErrorMessage(getDateTime() + "\n" + comment.get() + "\n\n" + deal.getErrorMessage());
                    deal.setStageId(StageEnum.SIN_STOCK.getValue());
                }
            }
        } else
            deal.setStageId(StageEnum.SEGUIMIENTO_DEL_PEDIDO.getValue());
        bitrixUtils.updateDeal(BitrixUpdate.builder()
                .id(String.valueOf(deal.getId()))
                .fields(deal)
                .build());
    }

    private void cancelOrder(BitrixDeal deal) {
        if (deal.getNoOrder() != null && !deal.getNoOrder().isEmpty()) {
            int numberOrder = Integer.parseInt(deal.getNoOrder());

            DocumentPed documentPed = documentPedRepository.findByNumero(numberOrder);
            DocumentPedHistory documentPedHistory = documentPedHistoryRepository.findByNumero(numberOrder);
            SoftJSDocumentPed softJSDocumentPed = softJSDocumentPedRepository.findByNumero(numberOrder);

            List<DocumentLinPed> documentLinPedList = documentLinPedRepository.findByNumero(documentPed.getNumero());
            List<SoftJSDocumentLinPed> softJSDocumentLinPeds = softJSDocumentLinPedRepository.findByNumero(documentPed.getNumero());

            documentLinPedRepository.deleteAllById(documentLinPedList.stream().map(DocumentLinPed::getId).toList());
            softJSDocumentLinPedRepository.deleteAllById(softJSDocumentLinPeds.stream().map(SoftJSDocumentLinPed::getId).toList());

            documentPed.setAnulado(1);
            documentPedRepository.save(documentPed);

            documentPedHistory.setAnulado(1);
            documentPedHistoryRepository.save(documentPedHistory);

            softJSDocumentPed.setAnulado('S');
            softJSDocumentPedRepository.save(softJSDocumentPed);
        }
    }

    public void paymentValidation(Payment model) throws BitrixException {
        if (model != null && model.getNotas() != null) {
            BitrixDeal deal = getDeal(Long.parseLong(model.getNotas())).getResult();
            if (model.getAutorizado() == 1) {
                system1320Repository.delete(System1320Data.builder()
                        .id(model.getId())
                        .build());

                Optional<System1320HistoryData> historyDataOptional = system1320HistoryRepository.findFirstByOrderByIdDesc();

                historyDataOptional.ifPresent(system1320HistoryData -> system1320HistoryRepository.save(System1320HistoryData.builder()
                        .id(system1320HistoryData.getId() + 1)
                        .nit(Long.parseLong(model.getNit()))
                        .mensaje(model.getMensaje())
                        .chat(model.getChat())
                        .usuario(model.getUsuario())
                        .usuario_autorizo(model.getUsuario_autorizo())
                        .autorizado(model.getAutorizado())
                        .Valor_Documento(model.getValor_Documento())
                        .fecha_hora(model.getFecha_hora())
                        .fecha_hora_a(model.getFecha_hora_a())
                        .programa("INTEGRACION BITRIX - " + model.getNotas() + " - " + (model.getItem() != null ? model.getItem() : "CLIENTE"))
                        .pc_a(model.getPc_a())
                        .build()));

                List<System1320Data> system1320Data = system1320Repository.findByNitAndNotas(Long.parseLong(model.getNit()), model.getNotas());

                if (system1320Data.isEmpty()) {
                    deal.setStageId(StageEnum.PEDIDO.getValue());
                    bitrixUtils.updateDeal(BitrixUpdate.builder()
                            .id(String.valueOf(deal.getId()))
                            .fields(deal)
                            .build());
                }
            } else if (model.getAutorizado() == 2) {
                system1320Repository.delete(System1320Data.builder()
                        .id(model.getId())
                        .build());

                Optional<System1320HistoryData> historyDataOptional = system1320HistoryRepository.findFirstByOrderByIdDesc();

                historyDataOptional.ifPresent(system1320HistoryData -> system1320HistoryRepository.save(System1320HistoryData.builder()
                        .id(system1320HistoryData.getId() + 1)
                        .nit(Long.parseLong(model.getNit()))
                        .chat(model.getChat())
                        .usuario(model.getUsuario())
                        .usuario_autorizo(model.getUsuario_autorizo())
                        .autorizado(model.getAutorizado())
                        .Valor_Documento(model.getValor_Documento())
                        .fecha_hora(model.getFecha_hora())
                        .fecha_hora_a(model.getFecha_hora_a())
                        .programa("INTEGRACION BITRIX - " + model.getNotas() + " - " + (model.getItem() != null ? model.getItem() : "CLIENTE"))
                        .pc_a(model.getPc_a())
                        .build()));

                deal.setStageId(StageEnum.SEGUIMIENTO_COTIZACION.getValue());
                deal.setErrorMessage(getDateTime() + "\nAutorizacion negada en: " + (model.getItem() != null ? model.getItem() : "CLIENTE") + " con el mensaje: " + model.getChat() + "\n" + deal.getErrorMessage());
                bitrixUtils.updateDeal(BitrixUpdate.builder()
                        .id(String.valueOf(deal.getId()))
                        .fields(deal)
                        .build());
            }
        }
    }

    public void startPrdProcess(PrdPlanProcess prdPlanProcess) {
        BitrixResult<List<BitrixDeal>> result = bitrixUtils.getDealByField(BitrixGetList.builder()
                        .filter(Map.of("UF_CRM_1743530021292", prdPlanProcess.getNumero()))
                        .build())
                .getBody();
        if (result != null && !result.getResult().isEmpty()) {
            BitrixDeal deal = result.getResult().getFirst();

            if (deal.getStageId().equals(StageEnum.SEGUIMIENTO_DEL_PEDIDO.getValue())) {
                deal.setStageId(StageEnum.EN_PRODUCCION.getValue());
                deal.setProductionEndDate(prdPlanProcess.getFechaEntregaCliente()
                        .atStartOfDay()
                        .atZone(ZoneId.of("America/Bogota"))
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                bitrixUtils.updateDeal(BitrixUpdate.builder()
                        .id(String.valueOf(deal.getId()))
                        .fields(deal)
                        .build());
            }
        }
    }

    public void updatePrdProcessStatus(PrdProcess prdProcess) {
        AtomicReference<String> details = new AtomicReference<>("");
        List<PrdProcessData> prdProcessData = prdProcessRepository.findByNumeroop(prdProcess.getNumeroOP());
        prdProcessData.forEach(prd -> {
            if (prd.getFechahorastart() != null && prd.getFechahorastop() != null && prd.getCodigo() != null)
                details.getAndUpdate(s -> s + prd.getCodigo() + ", Proceso: " + prd.getProceso().getDescripcion() + ", Actividad: " + prd.getActividad().getDescripcion() + " finalizada\n");
            else if (prd.getFechahorastart() != null && prd.getCodigo() != null)
                details.getAndUpdate(s -> s + prd.getCodigo() + ", Proceso: " + prd.getProceso().getDescripcion() + ", Actividad: " + prd.getActividad().getDescripcion() + " En progreso\n");
        });

        BitrixResult<List<BitrixDeal>> result = bitrixUtils.getDealByField(BitrixGetList.builder()
                        .filter(Map.of("UF_CRM_1743530021292", prdProcess.getNumeroOP()))
                        .build())
                .getBody();

        if (result != null && !result.getResult().isEmpty()) {
            BitrixDeal deal = result.getResult().getFirst();

            deal.setProductionDetails(details.get());

            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(deal.getId()))
                    .fields(deal)
                    .build());
        }
    }

    public void updateBillStatus(Bill bill) {
        if (bill.getTipo().startsWith("FVE")) {
            String document;
            BitrixResult<List<BitrixDeal>> result;
            if (bill.getDocumento().contains("-")) {
                document = bill.getDocumento().split("-")[1];
                result = bitrixUtils.getDealByField(BitrixGetList.builder()
                                .filter(Map.of("ID", document))
                                .build())
                        .getBody();
            } else {
                document = bill.getDocumento();
                result = bitrixUtils.getDealByField(BitrixGetList.builder()
                                .filter(Map.of("UF_CRM_1743530021292", document))
                                .build())
                        .getBody();
            }

            if (result != null && !result.getResult().isEmpty()) {
                BitrixDeal deal = result.getResult().getFirst();

                billStatusRepository.findById(Long.valueOf(document))
                        .ifPresent(bsd -> {
                            String details = Stream.of(bsd.getRemission(), bsd.getRemissionDate(), bsd.getBill(), bsd.getUser(), bsd.getConfirmDate())
                                    .filter(Objects::nonNull)
                                    .map(Object::toString)
                                    .collect(Collectors.joining(" "));
                            deal.setDetailsBill(details);
                            if (bsd.getRemissionDate() != null && bsd.getBillDate() == null)
                                deal.setStageId(StageEnum.APLAZADO.getValue());
                            else
                                deal.setStageId(StageEnum.FACTURADO.getValue());
                        });

                bitrixUtils.updateDeal(BitrixUpdate.builder()
                        .id(String.valueOf(deal.getId()))
                        .fields(deal)
                        .build());
            }
        }
    }

    public void createOrUpdateProduct(Product product) {
        migrationAppUtil.createOrUpdateProduct(product.getCodigo());
    }

    public void createOrUpdateCompany(Company company) {
        migrationAppUtil.createOrUpdateCompany(company.getNit());
    }

    public void createOrUpdateContact(Contact contact) {
        if (contact.getNit() != null && contact.getContacto() != null)
            migrationAppUtil.createOrUpdateContact(Customer.builder()
                    .companyId(Integer.valueOf(contact.getNit()))
                    .customerId(Integer.valueOf(contact.getContacto()))
                    .build());
    }

    private BitrixResult<BitrixDeal> getDeal(long id) throws BitrixException {
        try {
            return bitrixUtils.getDeal(id).getBody();
        } catch (HttpClientErrorException e) {
            BitrixError error = e.getResponseBodyAs(BitrixError.class);
            assert error != null;
            throw new BitrixException(error.getErrorDescription(), id, "deal");
        }
    }

    private BitrixResult<List<BitrixProductRows>> getDeadProducts(long id) throws BitrixException {
        try {
            return bitrixUtils.getDealProducts(id).getBody();
        } catch (HttpClientErrorException e) {
            BitrixError error = e.getResponseBodyAs(BitrixError.class);
            assert error != null;
            throw new BitrixException(error.getErrorDescription(), id, "dealProduct");
        }
    }

    private BitrixResult<BitrixCompany> getCompany(long id) throws BitrixException {
        try {
            return bitrixUtils.getCompany(id).getBody();
        } catch (HttpClientErrorException e) {
            BitrixError error = e.getResponseBodyAs(BitrixError.class);
            assert error != null;
            throw new BitrixException(error.getErrorDescription(), id, "company");
        }
    }

    private <T> void updateProductDeal(BitrixUpdate<T> bitrixUpdate) throws BitrixException {
        try {
            bitrixUtils.updateDealProduct(bitrixUpdate);
        } catch (HttpClientErrorException e) {
            BitrixError error = e.getResponseBodyAs(BitrixError.class);
            assert error != null;
            throw new BitrixException(error.getErrorDescription(), Long.parseLong(bitrixUpdate.getId()), "dealProducts");
        }
    }

    private String getDocumentNumber(String warehouse) {
        return "ZPE1" + "0".repeat(6 - warehouse.length()) + warehouse;
    }

    private String getDateTime() {
        return LocalDateTime.now(ZoneId.of("America/Bogota")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    private void getOtherUnit(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        if (deal.getOtherUnits().isEmpty()) {
            StringBuilder otherUnitItems = new StringBuilder();
            for (BitrixProductRows productRows : result.getResult()) {
                ProductData productData = productRepository.findByCodigo(productRows.getProductName());
                if (productData != null && productData.getManeja_otra_und() != null && productData.getManeja_otra_und().equals("S"))
                    otherUnitItems.append(productData.getCodigo()).append("_").append(productRows.getQuantity()).append(": /\n");
            }
            if (!otherUnitItems.isEmpty())
                otherUnitItems.deleteCharAt(otherUnitItems.lastIndexOf("/"));
            deal.setOtherUnits(otherUnitItems.toString());
        } else {
            Map<String, Integer> itemOtherUnit = getStringIntegerMap(deal);

            if (itemOtherUnit.entrySet().stream().anyMatch(s -> s.getValue().equals(0)))
                throw new BitrixException("Error valor(es) invalidos en segunda unidad", deal.getId(), "deal");

            StringBuilder otherUnitItems = new StringBuilder();
            StringBuilder otherUnitDetails = new StringBuilder();
            for (BitrixProductRows productRows : result.getResult()) {
                if (!itemOtherUnit.containsKey(productRows.getProductName() + "_" + productRows.getQuantity())) {
                    ProductData productData = productRepository.findByCodigo(productRows.getProductName());
                    if (productData != null && productData.getManeja_otra_und() != null && productData.getManeja_otra_und().equals("S"))
                        itemOtherUnit.put(productRows.getProductName() + "_" + productRows.getQuantity(), 0);
                }
                Integer other = itemOtherUnit.get(productRows.getProductName() + "_" + productRows.getQuantity());
                if (other != null) {
                    otherUnitDetails
                            .append(productRows.getProductName())
                            .append(": ")
                            .append(productRows.getQuantity())
                            .append(" ")
                            .append(productRows.getMeasure())
                            .append(" = ")
                            .append(other)
                            .append(" UND x ")
                            .append(Math.round((productRows.getQuantity() / other) * 10000.0) / 10000.0)
                            .append(" ")
                            .append(productRows.getMeasure())
                            .append(" C/U\n");
                    otherUnitItems.append(productRows.getProductName())
                            .append("_")
                            .append(productRows.getQuantity())
                            .append(": ")
                            .append(other)
                            .append(" /\n");
                }
            }
            if (!otherUnitItems.isEmpty())
                otherUnitItems.deleteCharAt(otherUnitItems.lastIndexOf("/"));
            deal.setOtherUnits(otherUnitItems.toString());
            deal.setDetailsOtherUnit(otherUnitDetails.toString());
        }

        bitrixUtils.updateDeal(BitrixUpdate.builder()
                .id(String.valueOf(deal.getId()))
                .fields(deal)
                .build());
    }

    private static Map<String, Integer> getStringIntegerMap(BitrixDeal deal) {
        Map<String, Integer> itemOtherUnit = new HashMap<>();
        String[] otherUnitList;
        if (deal.getOtherUnits().contains(":")) {
            otherUnitList = deal.getOtherUnits().strip().split("/");
            for (String s : otherUnitList) {
                String[] temp = s.split(":");
                if (temp.length > 1) {
                    String quantity = temp[1].replace(",", ".").replaceAll("\\..*$", "").replaceAll("\\D", "");
                    if (!quantity.isEmpty())
                        itemOtherUnit.put(temp[0].strip(), Integer.parseInt(quantity));
                    else
                        itemOtherUnit.put(temp[0].strip(), 0);
                } else {
                    itemOtherUnit.put(temp[0].strip(), 0);
                }
            }
        }
        return itemOtherUnit;
    }

    public String removeBBCode(String input) {
        return input.replaceAll("\\[(/)?[a-zA-Z]+(?:=[^]]*)?]", "");
    }

    @Scheduled(fixedRate = 10000) // Run every minute
    public void evictOldEntries() {
        log.debug("lock free run");
        long currentTime = System.currentTimeMillis();
        timedMap.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > EVICTION_DELAY_MS
        );
    }

    public void unexpectedError(long id, String message, String type) {
        log.error("{} - {} - {}", id, type, message);
        if (type.equals("deal")) {
            try {
                BitrixResult<BitrixDeal> bitrixResult = getDeal(id);
                BitrixDeal deal = bitrixResult.getResult();
                deal.setStageId(StageEnum.BANDEJA_DE_ENTRADA.getValue());
                deal.setErrorMessage(getDateTime() + "\n" + message + "\n\n" + deal.getErrorMessage());
                bitrixUtils.updateDeal(BitrixUpdate.builder()
                        .id(String.valueOf(deal.getId()))
                        .fields(deal)
                        .build());

                executorService.schedule(() -> timedMap.remove(id), 3, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
