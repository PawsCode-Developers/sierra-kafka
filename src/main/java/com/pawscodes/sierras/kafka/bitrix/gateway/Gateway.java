package com.pawscodes.sierras.kafka.bitrix.gateway;

import com.pawscodes.sierras.kafka.bitrix.data.*;
import com.pawscodes.sierras.kafka.bitrix.data.entity.*;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.DiscountCliId;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.StockDataId;
import com.pawscodes.sierras.kafka.bitrix.exception.BitrixException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Gateway {

    private static final long EVICTION_DELAY_MS = 60 * 1000;
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
    private final ConsecutiveRepository consecutiveRepository;
    private final DocumentPedRepository documentPedRepository;
    private final DiscountCliRepository discountCliRepository;
    private final FreightConfigRepository freightConfigRepository;
    private final DocumentLinPedRepository documentLinPedRepository;
    private final System1320HistoryRepository system1320HistoryRepository;
    private final SoftJSDocumentPedRepository softJSDocumentPedRepository;
    private final SoftJSDocumentLinPedRepository softJSDocumentLinPedRepository;
    private final DocumentLinPedHistoryRepository documentLinPedHistoryRepository;

    public Gateway(BitrixUtils bitrixUtils, MigrationAppUtil migrationAppUtil, UserRepository userRepository, StockRepository stockRepository, AuditRepository auditRepository, QuotaRepository quotaRepository, ProductRepository productRepository, CompanyRepository companyRepository, ConceptRepository conceptRepository, Concept2Repository concept2Repository, System1320Repository system1320Repository, PrdProcessRepository prdProcessRepository, CompanyDirRepository companyDirRepository, ConsecutiveRepository consecutiveRepository, DocumentPedRepository documentPedRepository, DiscountCliRepository discountCliRepository, FreightConfigRepository freightConfigRepository, DocumentLinPedRepository documentLinPedRepository, System1320HistoryRepository system1320HistoryRepository, SoftJSDocumentPedRepository softJSDocumentPedRepository, SoftJSDocumentLinPedRepository softJSDocumentLinPedRepository, DocumentLinPedHistoryRepository documentLinPedHistoryRepository) {
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
        this.consecutiveRepository = consecutiveRepository;
        this.documentPedRepository = documentPedRepository;
        this.discountCliRepository = discountCliRepository;
        this.freightConfigRepository = freightConfigRepository;
        this.documentLinPedRepository = documentLinPedRepository;
        this.system1320HistoryRepository = system1320HistoryRepository;
        this.softJSDocumentPedRepository = softJSDocumentPedRepository;
        this.softJSDocumentLinPedRepository = softJSDocumentLinPedRepository;
        this.documentLinPedHistoryRepository = documentLinPedHistoryRepository;
    }

    private boolean filterFunnel(BitrixDeal deal) {
        return Arrays.stream(StageEnum.values())
                .anyMatch(stageEnum -> stageEnum.getValue().equalsIgnoreCase(deal.getStageId()));
    }

    public void process(Long request) throws BitrixException {
        if (timedMap.get(request) == null) {
            timedMap.put(request, System.currentTimeMillis());
            BitrixResult<BitrixDeal> deal = getDeal(request);

            if (filterFunnel(deal.getResult())) {
                log.info("Deal {}, start processing", request);
                if (validateOrder(deal.getResult())) {
                    String stageId = deal.getResult().getStageId();
                    if (stageId.equals(StageEnum.BANDEJA_DE_ENTRADA.getValue()))
                        addClientInformation(deal.getResult());
                    else {
                        BitrixResult<List<BitrixProductRows>> result = validatePrice(deal.getResult());

                        if (stageId.equals(StageEnum.COTIZACION.getValue()))
                            processQuote(deal.getResult(), result);
                        else if (stageId.equals(StageEnum.PEDIDO.getValue()))
                            processOrder(deal.getResult(), result);
                        else if (stageId.equals(StageEnum.SEGUIMIENTO_DEL_PEDIDO.getValue()))
                            validateOrderPrice(deal.getResult(), result);
                        else if (stageId.equals(StageEnum.CERRADO_PERDIDO.getValue()))
                            cancelOrder(deal.getResult());
                    }
                }
                log.info("Deal process finish");
            }
            executorService.schedule(() -> {
                timedMap.remove(request);
            }, 2, TimeUnit.SECONDS);
        }
    }

    private void addClientInformation(BitrixDeal deal) throws BitrixException {
        if (deal.getAddresses().isEmpty() && deal.getCompanyId() != 0) {
            BitrixCompany company = getCompany(deal.getCompanyId()).getResult();
            List<CompanyDirData> companyDirData = companyDirRepository.findByNit((int) company.getNit());
            deal.setAddresses("");
            companyDirData.forEach(companyDirData1 -> {
                if (companyDirData1.getDir_activa() != null && companyDirData1.getDir_activa().equals("S"))
                    deal.setAddresses(deal.getAddresses() + "Dir: " + companyDirData1.getCodigoDireccion() + ", " + companyDirData1.getDireccion() + ", " + companyDirData1.getCiudad() + "\n");
            });

            companyDirData.stream()
                    .filter(companyDirData1 -> companyDirData1.getDir_activa().equals("S"))
                    .findFirst()
                    .ifPresent(companyDirData1 -> deal.setDeliveryAddress(companyDirData1.getDireccion()));

            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(deal.getId()))
                    .fields(deal)
                    .build());
        }
    }

    private void validateOrderPrice(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        AtomicBoolean hasToUpdateBitrixProducts = new AtomicBoolean(false);
        result.getResult().forEach(productRows -> {
            DocumentLinPed linPed = documentLinPedRepository.findByNumeroAndCodigo(Integer.parseInt(deal.getNoOrder()), productRows.getProductName());
            if (linPed != null) {
                if (linPed.getValorUnitario() != productRows.getPrice()) {
                    productRows.setPrice(linPed.getValorUnitario());
                    hasToUpdateBitrixProducts.set(true);
                }
                if (linPed.getCantidad() != productRows.getQuantity()) {
                    linPed.setCantidad(productRows.getQuantity());
                    documentLinPedRepository.save(linPed);
                }
            } else {
                DocumentLinPed last = documentLinPedRepository.findLastByNumero(Integer.parseInt(deal.getNoOrder()));
                documentLinPedRepository.save(DocumentLinPed.builder()
                        .seq(last.getSeq() + 1)
                        .codigo(productRows.getProductName())
                        .cantidad(productRows.getQuantity())
                        .valorUnitario(productRows.getPrice())
                        .numero(Integer.parseInt(deal.getNoOrder()))
                        .bodega(last.getBodega())
                        .porcentaje_iva(productRows.getTax())
                        .und(productRows.getMeasure())
                        .despacho_virtual(0)
                        .build());
            }
        });

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

        if (result.getResult().isEmpty()) {
            deal.setErrorMessage("No tiene productos");
            deal.setStageId(StageEnum.BANDEJA_DE_ENTRADA.getValue());
            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(deal.getId()))
                    .fields(deal)
                    .build());
        } else {
            for (BitrixProductRows productRows : result.getResult()) {
                ProductData productData = productRepository.findById(productRows.getProductName())
                        .orElseGet(ProductData::new);
                if (deal.getCompanyId() != 0) {
                    BitrixResult<BitrixCompany> company = getCompany(deal.getCompanyId());
                    Optional<DiscountCli> discountCli = discountCliRepository.findById(new DiscountCliId(company.getResult().getNit(), productData.getCodigo()));
                    if (discountCli.isPresent()) {
                        if (deal.getStageId().equals(StageEnum.COTIZACION.getValue()) && deal.getDiscountDetails().isEmpty()) {
                            double discount = (double) Math.round(discountCli.get().getDescuento() * 100) / 100;
                            double newPrice = productRows.getOriginalPrice() * (1 - (discount / 100));
                            productRows.setOriginalPrice(newPrice);
                            productRows.setPriceNet(newPrice);
                            productRows.setBrutePrice(newPrice);
                            productRows.setPrice(newPrice * (1 - (productRows.getDiscountRate() / 100)));
                            deal.setDiscountDetails(deal.getDiscountDetails() + productData.getCodigo() + ": " + discount + "%" + "\n");
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
                } else {
                    comment = new StringBuilder("No hay compañia asignada");
                    newToUpdate = true;
                }
                bitrixProductRows.add(productRows);
            }

            if (newToUpdate) {
                if ((comment.compareTo(new StringBuilder("Productos con el precio incorrecto:\n")) == 0)) {
                    comment = new StringBuilder();
                }

                if (!deal.getDiscountDetails().isEmpty()) {
                    updateProductDeal(BitrixUpdate.<BitrixProductRows>builder()
                            .id(String.valueOf(deal.getId()))
                            .rows(bitrixProductRows)
                            .build());
                }

                deal.setErrorMessage(comment.toString());
                bitrixUtils.updateDeal(BitrixUpdate.builder()
                        .id(String.valueOf(deal.getId()))
                        .fields(deal)
                        .build());
            }
        }

        return result;
    }

    private boolean validateProductPrice(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result, CompanyData company) {
        List<System1320HistoryData> system1320HistoryData = system1320HistoryRepository
                .findByNitAndPrograma(company.getNit(), "INTEGRACION BITRIX - " + deal.getId());

        if (!system1320HistoryData.isEmpty()) {
            return system1320HistoryData.stream()
                    .map(System1320HistoryData::getAutorizado)
                    .distinct()
                    .count() > 1;
        } else {
            boolean needAutorization = false;
            BitrixResult<List<BitrixUser>> userList = bitrixUtils.getUser(BitrixGetList.builder()
                            .filter(Map.of("ID", deal.getAssigned()))
                            .build())
                    .getBody();
            UserData userData = userRepository.findByCorreointbitrix(userList.getResult().getFirst().getEmail());

            BitrixResult<List<BitrixCustomFields>> customFields = bitrixUtils.getDealCustomFields(BitrixGetList.builder()
                            .filter(Map.of("FIELD_NAME", "UF_CRM_1743439978"))
                            .build())
                    .getBody();

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
                ProductData productData = productRepository.findById(productRows.getProductName())
                        .orElseGet(ProductData::new);
                Optional<DiscountCli> discountCli = discountCliRepository.findById(new DiscountCliId(company.getNit(), productData.getCodigo()));
                if (discountCli.isPresent()) {
                    if (productRows.getPrice() < (productData.getPrecio_si_costo_cero() * (1 - (discountCli.get().getDescuento() / 100)))) {
                        system1320Repository.save(System1320Data.builder()
                                .item(productRows.getProductName())
                                .usuario(userData != null ? userData.getUsuario() : "BITRIX")
                                .condpagocliente(Integer.parseInt(company.getCondicion()))
                                .Valor_Documento(deal.getAmount())
                                .nit(company.getNit())
                                .condpagocliente(Integer.parseInt(company.getCondicion()))
                                .notas(String.valueOf(deal.getId()))
                                .mensaje("Valor de venta menor al permitido")
                                .bodega(warehouse)
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
                            .condpagocliente(Integer.parseInt(company.getCondicion()))
                            .notas(String.valueOf(deal.getId()))
                            .mensaje("Valor de venta menor al permitido")
                            .bodega(warehouse)
                            .build());
                    needAutorization = true;
                }
            }
            return needAutorization;
        }
    }

    private void processQuote(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        if (result.getResult().stream().noneMatch(bitrixProductRows -> bitrixProductRows.getProductName().contains("FLETES"))) {
            String stage = StageEnum.SEGUIMIENTO_COTIZACION.getValue();
            String comments = "";
            double price = 0;
            if (result.getResult().isEmpty()) {
                stage = StageEnum.BANDEJA_DE_ENTRADA.getValue();
                comments = "No se encontro ningun producto";
            } else {
                boolean isChemist = false;
                for (BitrixProductRows productRows : result.getResult()) {
                    price += productRows.getPrice() * productRows.getQuantity();
                }

                BitrixResult<BitrixCompany> company = getCompany(deal.getCompanyId());
                CompanyData companyData = companyRepository.findByNit(company.getResult().getNit());

                BitrixResult<List<BitrixCustomFields>> customFields = bitrixUtils.getDealCustomFields(BitrixGetList.builder()
                                .filter(Map.of("FIELD_NAME", "UF_CRM_1744648899"))
                                .build())
                        .getBody();

                Map<Integer, String> freightTypes = customFields.getResult()
                        .getFirst()
                        .getValues()
                        .stream()
                        .collect(Collectors.toMap(BitrixCustomFields.ListItem::getId, BitrixCustomFields.ListItem::getValue));

                int deliveryCode = 1;
                if (deal.getDeliveryAddress().startsWith("Dir:")) {
                    String s = deal.getDeliveryAddress().strip().split(",")[0].strip().split(":")[1].replace(" ", "");
                    deliveryCode = Integer.parseInt(s);
                }
                CompanyDirData companyDirData = companyDirRepository.findByNitAndCodigoDireccion(companyData.getNit(), deliveryCode);
                FreightConfigData freightConfigData;
                if (companyDirData != null) {
                    freightConfigData = freightConfigRepository.findByTipofleteAndCategoriaclienteAndPaisAndDepartamentoAndCiudad(
                            freightTypes.get(deal.getFreightType()).substring(0, 1),
                            companyData.getConcepto_14(),
                            companyDirData.getY_pais(),
                            companyDirData.getY_dpto(),
                            companyDirData.getY_ciudad());
                } else {
                    freightConfigData = null;
                }

                if (freightConfigData != null && freightConfigData.getValorminimoventa() > price) {
                    Optional<ProductData> productData = productRepository.findByFreight().stream()
                            .filter(p -> p.getValor_unitario() == freightConfigData.getValorflete())
                            .findFirst();

                    if (productData.isPresent()) {
                        BitrixResult<Map<String, List<BitrixGetProduct>>> mapBitrixResult = bitrixUtils.getProductByFilter(BitrixGetList.builder()
                                        .select(List.of("id", "iblockId"))
                                        .filter(Map.of("iblockId", 14, "code", productData.get().getCodigo()))
                                        .build())
                                .getBody();

                        result.getResult().add(BitrixProductRows.builder()
                                .productId(mapBitrixResult.getResult().get("products").getFirst().getId())
                                .productName(productData.get().getCodigo())
                                .originalPrice(productData.get().getValor_unitario())
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
            deal.setErrorMessage(comments);
            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(deal.getId()))
                    .fields(deal)
                    .build());
        }
    }

    private boolean validateQuota(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        BitrixResult<BitrixCompany> company = getCompany(deal.getCompanyId());
        List<System1320HistoryData> system1320HistoryData = system1320HistoryRepository
                .findByNitAndPrograma(company.getResult().getNit(), "INTEGRACION BITRIX - " + deal.getId());

        if (!system1320HistoryData.isEmpty()) {
            return system1320HistoryData.stream()
                    .map(System1320HistoryData::getAutorizado)
                    .distinct()
                    .count() <= 1;
        } else {
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
                totalPending.updateAndGet(v -> v + quotaData.getVencida());
                totalPassDue.updateAndGet(v -> v + quotaData.getVencida());
            });

            // use BigDecimal for count money
            // cast decimals to 2
            boolean needUpdate = false;
            String message = "";
            if (companyData.getCondicion().equals("0") || companyData.getCondicion().equals("00")) {
                message = "El Cliente paga de contado";
                needUpdate = true;
            } else if (totalPassDue.get() > 0) {
                message = "Cliente en mora por valor de: " + totalPassDue.get().toString();
                needUpdate = true;
            } else if (totalPending.get() > 0 && total.get() + price > companyData.getCupo_credito()) {
                message = "El Cliente no cuenta con cupo suficiente";
                needUpdate = true;
            }

            if (needUpdate) {
                BitrixResult<List<BitrixUser>> userList = bitrixUtils.getUser(BitrixGetList.builder()
                                .filter(Map.of("ID", deal.getAssigned()))
                                .build())
                        .getBody();
                UserData userData = userRepository.findByCorreointbitrix(userList.getResult().getFirst().getEmail());

                system1320Repository.save(System1320Data.builder()
                        .usuario(userData != null ? userData.getUsuario() : "BITRIX")
                        .condpagocliente(Integer.parseInt(companyData.getCondicion()))
                        .Valor_Documento(deal.getAmount())
                        .nit(company.getResult().getNit())
                        .condpagocliente(Integer.parseInt(companyData.getCondicion()))
                        .notas(String.valueOf(deal.getId()))
                        .mensaje(message)
                        .bodega(0)
                        .build());

                deal.setErrorMessage(message);
                deal.setStageId(StageEnum.VALIDACION_PAGO_CUPO.getValue());
                bitrixUtils.updateDeal(BitrixUpdate.builder()
                        .id(String.valueOf(deal.getId()))
                        .fields(deal)
                        .build());
                return false;
            }
            return price != 0;
        }
    }

    @Transactional
    private void processOrder(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        if (deal.getNoOrder().isEmpty() && deal.getCompanyId() != 0) {
            BitrixResult<BitrixCompany> company = getCompany(deal.getCompanyId());
            CompanyData companyData = companyRepository.findByNit(company.getResult().getNit());
            if (result.getResult().isEmpty()) {
                deal.setStageId(StageEnum.BANDEJA_DE_ENTRADA.getValue());
                deal.setErrorMessage("No tiene productos");
            } else if (validateProductPrice(deal, result, companyData)) {
                deal.setStageId(StageEnum.VALIDACION_PAGO_CUPO.getValue());
            } else if (validateQuota(deal, result)) {
                BitrixResult<List<BitrixCustomFields>> customFields = bitrixUtils.getDealCustomFields(BitrixGetList.builder()
                                .build())
                        .getBody();

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

                Optional<ConsecutiveData> consecutiveDataOptional = consecutiveRepository.findById(getDocumentNumber(warehouse));
                if (consecutiveDataOptional.isPresent()) {
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
                            .notas(deal.getComments())
                            .usuario(userData != null ? userData.getUsuario() : "BITRIX")
                            .Nit_Usuario(String.valueOf(userData != null ? userData.getNit() : 0))
                            .concepto(concept)
                            .concepto2(concept2)
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
                            .notas(deal.getComments())
                            .usuario(userData != null ? userData.getUsuario() : "BITRIX")
                            .usuarioactualizo(userData != null ? userData.getUsuario() : "BITRIX")
                            .fechahoraactualizo(documentPed.getFecha_hora())
                            .concepto(documentPed.getConcepto())
                            .concepto2(documentPed.getConcepto2())
                            .build());

                    result.getResult().forEach(bitrixProductRows -> {
                        boolean hasStock = true;
                        Optional<StockData> stockDataList = stockRepository.findById(new StockDataId(
                                bitrixProductRows.getProductName(),
                                warehouse,
                                date.getYear(),
                                date.getMonthValue()
                        ));

                        if (stockDataList.isPresent()) {
                            if (stockDataList.get().getStock() < bitrixProductRows.getQuantity())
                                hasStock = false;
                        }

                        if (!hasStock)
                            comment.getAndUpdate(s -> s + bitrixProductRows.getProductName() + "\n");
                        else {
                            DocumentLinPed documentLinPed = documentLinPedRepository.save(DocumentLinPed.builder()
                                    .seq(seq.getAndAdd(1))
                                    .codigo(bitrixProductRows.getProductName())
                                    .cantidad(bitrixProductRows.getQuantity())
                                    .valorUnitario(bitrixProductRows.getPrice())
                                    .numero(consecutiveDataOptional.get().getNext())
                                    .bodega(Integer.parseInt(warehouse))
                                    .porcentaje_iva(bitrixProductRows.getTax())
                                    .und(bitrixProductRows.getMeasure())
                                    .despacho_virtual(0)
                                    .build());

                            SoftJSDocumentLinPed softJSDocumentLinPed = softJSDocumentLinPedRepository.save(SoftJSDocumentLinPed.builder()
                                    .seq(documentLinPed.getSeq())
                                    .bodega(documentLinPed.getBodega())
                                    .iddoclinped(documentLinPed.getId())
                                    .cantidad(documentLinPed.getCantidad())
                                    .numero(documentLinPed.getNumero())
                                    .valorunitario(documentLinPed.getValorUnitario())
                                    .codigo(bitrixProductRows.getProductName())
                                    .notas(deal.getComments())
                                    .porcentajeiva(documentLinPed.getPorcentaje_iva())
                                    .und(documentLinPed.getUnd())
                                    .idsdp(softJSDocumentPed.getId())
                                    .build());
                            log.debug("Last record: {}", softJSDocumentLinPed.getId());
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

                if (comment.compareAndSet("Productos sin existencia:\n", ""))
                    deal.setStageId(stage);
                else {
                    deal.setErrorMessage(comment.get());
                    deal.setStageId(StageEnum.VALIDACION_PAGO_CUPO.getValue());
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
        String numberOrder = deal.getNoOrder();

        DocumentPed documentPed = documentPedRepository.findByNumero(Integer.parseInt(numberOrder));

        List<DocumentLinPed> documentLinPedList = documentLinPedRepository.findByNumero(documentPed.getNumero());
        documentLinPedList.forEach(documentLinPed -> documentLinPedHistoryRepository.save(DocumentLinPedHistory.builder()
                .id(documentLinPedHistoryRepository.findTop1ByOrderByIdDesc().getId() + 1)
                .sw(documentLinPed.getSw())
                .adicional(documentLinPed.getAdicional())
                .cantidad_despachada(documentLinPed.getCantidad_despachada())
                .cantidad_und(documentLinPed.getCantidad_und())
                .porc_dcto_2(documentLinPed.getPorc_dcto_2())
                .porc_dcto_3(documentLinPed.getPorc_dcto_3())
                .porcentaje_descuento(documentLinPed.getPorcentaje_descuento())
                .bodega(documentLinPed.getBodega())
                .cantidad(documentLinPed.getCantidad())
                .codigo(documentLinPed.getCodigo())
                .despacho_virtual(documentLinPed.getDespacho_virtual())
                .seq(documentLinPed.getSeq())
                .und(documentLinPed.getUnd())
                .porcentaje_iva(documentLinPed.getPorcentaje_iva())
                .valorUnitario(documentLinPed.getValorUnitario())
                .numero(documentLinPed.getNumero())
                .build()));

        documentLinPedRepository.deleteAllById(documentLinPedList.stream().map(DocumentLinPed::getId).toList());

        documentPed.setAnulado(1);
        documentPedRepository.save(documentPed);
    }

    public void paymentValidation(Payment model) throws BitrixException {
        if (model.getNotas() != null) {
            BitrixDeal deal = getDeal(Long.parseLong(model.getNotas())).getResult();
            if (model.getAutorizado() == 1) {
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
                        .programa("INTEGRACION BITRIX - " + model.getNotas())
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
                deal.setErrorMessage("Autorizacion negada en: " + model.getItem() + " con el mensaje: " + model.getChat());
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
            details.getAndUpdate(s -> s + prd.getCodigo() + ", Proceso: " + prd.getProceso().getDescripcion() + ", Actividad: " + prd.getActividad().getDescripcion() + "\n");
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

    public void createOrUpdateProduct(Product product) {
        migrationAppUtil.createOrUpdateProduct(product.getCodigo());
    }

    public void createOrUpdateCompany(Company company) {
        migrationAppUtil.createOrUpdateCompany(company.getNit());
    }

    public void createOrUpdateContact(Contact contact) {
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
            throw new BitrixException(error.getErrorDescription());
        }
    }

    private BitrixResult<List<BitrixProductRows>> getDeadProducts(long id) throws BitrixException {
        try {
            return bitrixUtils.getDealProducts(id).getBody();
        } catch (HttpClientErrorException e) {
            BitrixError error = e.getResponseBodyAs(BitrixError.class);
            throw new BitrixException(error.getErrorDescription());
        }
    }

    private BitrixResult<BitrixCompany> getCompany(long id) throws BitrixException {
        try {
            return bitrixUtils.getCompany(id).getBody();
        } catch (HttpClientErrorException e) {
            BitrixError error = e.getResponseBodyAs(BitrixError.class);
            throw new BitrixException(error.getErrorDescription());
        }
    }

    private <T> void updateProductDeal(BitrixUpdate<T> bitrixUpdate) throws BitrixException {
        try {
            bitrixUtils.updateDealProduct(bitrixUpdate)
                    .getBody();
        } catch (HttpClientErrorException e) {
            BitrixError error = e.getResponseBodyAs(BitrixError.class);
            throw new BitrixException(error.getErrorDescription());
        }
    }

    private String getDocumentNumber(String warehouse) {
        return "ZPE1" + "0".repeat(6 - warehouse.length()) + warehouse;
    }

    @Scheduled(fixedRate = 10000) // Run every minute
    public void evictOldEntries() {
        log.debug("lock free run");
        long currentTime = System.currentTimeMillis();
        timedMap.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > EVICTION_DELAY_MS
        );
    }
}
