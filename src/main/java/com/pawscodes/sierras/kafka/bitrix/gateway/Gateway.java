package com.pawscodes.sierras.kafka.bitrix.gateway;

import com.pawscodes.sierras.kafka.bitrix.data.*;
import com.pawscodes.sierras.kafka.bitrix.data.entity.*;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.DiscountCliId;
import com.pawscodes.sierras.kafka.bitrix.data.entity.composeId.StockDataId;
import com.pawscodes.sierras.kafka.bitrix.exception.BitrixException;
import com.pawscodes.sierras.kafka.bitrix.model.bitrix.*;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Company;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Contact;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Payment;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Product;
import com.pawscodes.sierras.kafka.bitrix.util.BitrixUtils;
import com.pawscodes.sierras.kafka.bitrix.util.MigrationAppUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Gateway {
    private final BitrixUtils bitrixUtils;
    private final MigrationAppUtil migrationAppUtil;

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final ConceptRepository conceptRepository;
    private final Concept2Repository concept2Repository;
    private final ConsecutiveRepository consecutiveRepository;
    private final DocumentPedRepository documentPedRepository;
    private final DiscountCliRepository discountCliRepository;
    private final DocumentLinPedRepository documentLinPedRepository;
    private final SoftJSDocumentPedRepository softJSDocumentPedRepository;
    private final SoftJSDocumentLinPedRepository softJSDocumentLinPedRepository;

    public Gateway(BitrixUtils bitrixUtils, MigrationAppUtil migrationAppUtil, UserRepository userRepository, StockRepository stockRepository, ProductRepository productRepository, CompanyRepository companyRepository, ConceptRepository conceptRepository, Concept2Repository concept2Repository, ConsecutiveRepository consecutiveRepository, DocumentPedRepository documentPedRepository, DiscountCliRepository discountCliRepository, DocumentLinPedRepository documentLinPedRepository, SoftJSDocumentPedRepository softJSDocumentPedRepository, SoftJSDocumentLinPedRepository softJSDocumentLinPedRepository) {
        this.bitrixUtils = bitrixUtils;
        this.migrationAppUtil = migrationAppUtil;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.conceptRepository = conceptRepository;
        this.concept2Repository = concept2Repository;
        this.consecutiveRepository = consecutiveRepository;
        this.documentPedRepository = documentPedRepository;
        this.discountCliRepository = discountCliRepository;
        this.documentLinPedRepository = documentLinPedRepository;
        this.softJSDocumentPedRepository = softJSDocumentPedRepository;
        this.softJSDocumentLinPedRepository = softJSDocumentLinPedRepository;
    }

    private boolean filterFunnel(BitrixDeal deal) {
        return Arrays.stream(StageEnum.values())
                .anyMatch(stageEnum -> stageEnum.getValue().equalsIgnoreCase(deal.getStageId()));
    }

    public void process(Long request) throws BitrixException {
        BitrixResult<BitrixDeal> deal = getDeal(request);

        if (filterFunnel(deal.getResult())) {
            log.info("Deal {}, start processing", request);

            if (validateOrder(deal.getResult())) {
                String stageId = deal.getResult().getStageId();
                if (!stageId.equals(StageEnum.BANDEJA_DE_ENTRADA.getValue())) {
                    BitrixResult<List<BitrixProductRows>> result = validatePrice(deal.getResult());

                    if (stageId.equals(StageEnum.COTIZACION.getValue()))
                        processQuote(deal.getResult(), result);
                    else if (stageId.equals(StageEnum.PEDIDO.getValue()))
                        processOrder(deal.getResult(), result);
                    else if (stageId.equals(StageEnum.SEGUIMIENTO_DEL_PEDIDO.getValue()))
                        validateOrderPrice(deal.getResult(), result);
                }
            }
            log.info("Deal process finish");
        }
    }

    private void validateOrderPrice(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        AtomicBoolean hasToUpdateBitrixProducts = new AtomicBoolean(false);
        AtomicReference<Double> totalPrice = new AtomicReference<>((double) 0);
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
                totalPrice.updateAndGet(v -> v + (linPed.getValorUnitario() * linPed.getCantidad()));
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

        if (!totalPrice.get().equals(deal.getAmount())) {
            deal.setAmount(totalPrice.get());
            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(deal.getId()))
                    .fields(deal)
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
        List<BitrixProductRows> bitrixProductRows = new ArrayList<>();
        StringBuilder comment = new StringBuilder("Productos con el precio incorrecto:\n");

        if (result.getResult().isEmpty()) {
            deal.setErrorMessage("No tiene productos");
            deal.setStageId(StageEnum.BANDEJA_DE_ENTRADA.getValue());
            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(deal.getId()))
                    .fields(deal)
                    .build());
        } else {
            for (BitrixProductRows productRows : result.getResult()) {
                double realPrice = 0;
                ProductData productData = productRepository.findById(productRows.getProductName())
                        .orElseGet(ProductData::new);
                if (deal.getCompanyId() != 0) {
                    BitrixResult<BitrixCompany> company = getCompany(deal.getCompanyId());
                    Optional<DiscountCli> discountCli = discountCliRepository.findById(new DiscountCliId(company.getResult().getNit(), productData.getCodigo()));
                    if (discountCli.isPresent()) {
                        if (discountCli.get().getDescuento() < productRows.getDiscountRate()) {
                            comment.append("Producto: ").append(productRows.getProductName()).append(" tiene un mayor descuento\n");
                            realPrice = productData.getValor_unitario();
                        }
                        if (productRows.getPrice() < (productData.getCosto_unitario() * (1 - (discountCli.get().getDescuento() / 100)))) {
                            comment.append("Producto: ").append(productRows.getProductName()).append(" tiene un menor precio del permitido\n");
                            realPrice = productData.getValor_unitario();
                        }
                    } else if (productRows.getPrice() < productData.getCosto_unitario()) {
                        comment.append("Producto: ").append(productRows.getProductName()).append(" tiene un menor precio del permitido\n");
                        realPrice = productData.getValor_unitario();
                    }
                } else {
                    comment = new StringBuilder("No hay compañia asignada");
                }

                if (realPrice != 0) {
                    productRows.setPrice(realPrice);
                    productRows.setDiscountRate(0); // descuento revisar
                }
                productRows.setDiscountRate(productRows.getDiscountRate());
                bitrixProductRows.add(productRows);
            }

            if ((comment.compareTo(new StringBuilder("Productos con el precio incorrecto:\n")) != 0)) {
                updateProductDeal(BitrixUpdate.<BitrixProductRows>builder()
                        .id(String.valueOf(deal.getId()))
                        .rows(bitrixProductRows)
                        .build());

                deal.setStageId(StageEnum.BANDEJA_DE_ENTRADA.getValue());
                deal.setAmount(0);
                deal.setErrorMessage(comment.toString());
                bitrixUtils.updateDeal(BitrixUpdate.builder()
                        .id(String.valueOf(deal.getId()))
                        .fields(deal)
                        .build());
            }
        }

        return result;
    }

    private void processQuote(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) {
        String stage = StageEnum.SEGUIMIENTO_COTIZACION.getValue();
        String comments = "";
        if (result.getResult().isEmpty()) {
            stage = StageEnum.BANDEJA_DE_ENTRADA.getValue();
            comments = "No se encontro ningun producto";
        }
        double price = 0;
        for (BitrixProductRows productRows : result.getResult()) {
            price += productRows.getPrice() * productRows.getQuantity();
        }
        deal.setStageId(stage);
        deal.setAmount(price);
        deal.setErrorMessage(comments);
        bitrixUtils.updateDeal(BitrixUpdate.builder()
                .id(String.valueOf(deal.getId()))
                .fields(deal)
                .build());
    }

    @Transactional
    private void processOrder(BitrixDeal deal, BitrixResult<List<BitrixProductRows>> result) throws BitrixException {
        if (deal.getNoOrder().isEmpty()) {
            BitrixResult<List<BitrixCustomFields>> customFields = bitrixUtils.getDealCustomFields(BitrixGetList.builder()
                            .build())
                    .getBody();

            Map<String, Map<Integer, String>> fieldsValues = customFields.getResult().stream()
                    .filter(bitrixCustomFields -> bitrixCustomFields.getFieldName().matches("UF_CRM_1743439978|UF_CRM_1743774849|UF_CRM_1743774680"))
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

            BitrixResult<List<BitrixUser>> userList = bitrixUtils.getUser(BitrixGetList.builder()
                            .filter(deal.getAssigned())
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
            BitrixResult<BitrixCompany> company = getCompany(deal.getCompanyId());
            CompanyData companyData = companyRepository.findByNit(company.getResult().getNit());
            Optional<ConsecutiveData> consecutiveDataOptional = consecutiveRepository.findById(getDocumentNumber(warehouse));
            if (consecutiveDataOptional.isPresent()) {
                DocumentPed documentPed = documentPedRepository.save(DocumentPed.builder()
                        .numero(consecutiveDataOptional.get().getNext())
                        .bodega(Integer.parseInt(warehouse))
                        .nit(companyData.getNit())
                        .valor_total(deal.getAmount())
                        .fecha(date)
                        .fecha_hora(LocalDateTime.now())
                        .vendedor(userData.getNit())
                        .diasValidez(Integer.parseInt(deal.getValidDay().split(" ")[0]))
                        .condicion(companyData.getCondicion())
                        .notas(deal.getComments())
                        .usuario(userData.getUsuario())
                        .Nit_Usuario(String.valueOf(userData.getNit()))
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
                        .usuario(userData.getUsuario())
                        .usuarioactualizo(userData.getUsuario())
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

                deal.setNoOrder(String.valueOf(documentPed.getNumero()));

                consecutiveRepository.save(ConsecutiveData.builder()
                        .type(consecutiveDataOptional.get().getType())
                        .next(consecutiveDataOptional.get().getNext() + 1)
                        .build());
            }

            if (comment.compareAndSet("Productos sin existencia:\n", ""))
                deal.setStageId(stage);
            else {
                deal.setErrorMessage(comment.get());
                deal.setStageId(StageEnum.SEGUIMIENTO_COTIZACION.getValue());
            }
        } else
            deal.setStageId(StageEnum.SEGUIMIENTO_DEL_PEDIDO.getValue());
        bitrixUtils.updateDeal(BitrixUpdate.builder()
                .id(String.valueOf(deal.getId()))
                .fields(deal)
                .build());
    }

    public void paymentValidation(Payment model) {
        BitrixResult<List<BitrixDeal>> bitrixResult = bitrixUtils.getDeadByField(BitrixGetList.builder()
                        .filter(Map.of("UF_CRM_1741043732565", model.getNit(),
                                "STAGE_ID", StageEnum.VALIDACION_PAGO_CUPO.getValue()))
                        .build())
                .getBody();

        if (bitrixResult != null && bitrixResult.getResult().size() == 1) {
            BitrixDeal dead = bitrixResult.getResult().getFirst();
            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(dead.getId()))
                    .fields(BitrixDeal.builder()
                            .contactId(dead.getContactId())
                            .companyId(dead.getCompanyId())
                            .stageId(StageEnum.PEDIDO.getValue())
                            .amount(dead.getAmount())
                            .comments("Pago Validado")
                            .build())
                    .build());
        } else {
            log.error("");
        }
    }

    public void createOrUpdateProduct(Product product) {
        migrationAppUtil.createOrUpdateProduct(product.getCodigo());
    }

    public void createOrUpdateCompany(Company company) {
        migrationAppUtil.createOrUpdateContact(company.getNit());
    }

    public void createOrUpdateContact(Contact contact) {

    }

    private BitrixResult<BitrixDeal> getDeal(long id) throws BitrixException {
        try {
            return bitrixUtils.getDead(id).getBody();
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
}
