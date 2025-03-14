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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Gateway {

    private final BitrixUtils bitrixUtils;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final DocumentPedRepository documentPedRepository;
    private final DiscountCliRepository discountCliRepository;
    private final SoftJSDocumentPedRepository softJSDocumentPedRepository;

    public Gateway(BitrixUtils bitrixUtils, StockRepository stockRepository, ProductRepository productRepository, DocumentPedRepository documentPedRepository, DiscountCliRepository discountCliRepository, SoftJSDocumentPedRepository softJSDocumentPedRepository) {
        this.bitrixUtils = bitrixUtils;
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.documentPedRepository = documentPedRepository;
        this.discountCliRepository = discountCliRepository;
        this.softJSDocumentPedRepository = softJSDocumentPedRepository;
    }

    public void process(Long request) throws BitrixException {
        BitrixResult<BitrixDead> dead = getDead(request);

        if (dead.getResult().getStageId().equals(StageEnum.BANDEJA_DE_ENTRADA.getValue()) ||
                dead.getResult().getStageId().equals(StageEnum.COTIZACION.getValue())) {
            processQuote(dead.getResult());
        } else if (dead.getResult().getStageId().equals(StageEnum.PEDIDO.getValue())) {
            processOrder(dead.getResult());
        }
    }

    private void processQuote(BitrixDead dead) throws BitrixException {
        BitrixResult<List<BitrixProductRows>> result = getDeadProducts(dead.getId());

        if (dead.getComments().isEmpty()) {
            double price = 0;
            long nit = 0L;
            StringBuilder comment = new StringBuilder("Productos on el precio erroneo:\n");
            for (int i = 0; i < result.getResult().size(); i++) {
                BitrixProductRows productRows = result.getResult().get(i);
                ProductData productData = productRepository.findById(productRows.getProductName())
                        .orElseGet(ProductData::new);
                if (dead.getCompanyId() != 0) {
                    BitrixResult<BitrixCompany> company = getCompany(dead.getCompanyId());
                    nit = company.getResult().getNit();
                    Optional<DiscountCli> discountCli = discountCliRepository.findById(new DiscountCliId(company.getResult().getNit(), productData.getCodigo()));
                    if (discountCli.isPresent()) {
                        if (productRows.getPrice() < (productData.getCosto_unitario() * (1 - (discountCli.get().getDescuento() / 100))))
                            comment.append("Producto: ").append(productRows.getProductName()).append(" tiene un menor precio del permitido\n");
                    } else if (productRows.getPrice() < productData.getCosto_unitario()) {
                        comment.append("Producto: ").append(productRows.getProductName()).append(" tiene un menor precio del permitido\n");
                    }
                } else {
                    comment = new StringBuilder("No hay compañia asignada");
                }
                price += productRows.getPrice() * productRows.getQuantity();
            }

            String stage = StageEnum.SEGUIMIENTO_COTIZACION.getValue();
            if ((comment.compareTo(new StringBuilder("Productos on el precio erroneo:\n")) != 0)) {
                stage = StageEnum.COTIZACION.getValue();
                price = 0;
            } else
                comment = new StringBuilder();

            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(dead.getId()))
                    .fields(BitrixDead.builder()
                            .contactId(dead.getContactId())
                            .companyId(dead.getCompanyId())
                            .stageId(stage)
                            .amount(price)
                            .comments(comment.toString())
                            .nitPay(String.valueOf(nit))
                            .build())
                    .build());
        }
    }

    private void processOrder(BitrixDead dead) throws BitrixException {
        BitrixResult<List<BitrixProductRows>> result = getDeadProducts(dead.getId());

        AtomicReference<String> comment = new AtomicReference<>("Productos sin existencia:\n");
        LocalDate date = LocalDate.now();
        result.getResult().forEach(bitrixProductRows -> {
            Optional<StockData> stockData = stockRepository.findById(new StockDataId(
                    bitrixProductRows.getProductName(),
                    "1",
                    date.getYear(),
                    date.getMonthValue())
            );
            if (stockData.isPresent()) {
                if (bitrixProductRows.getQuantity() > stockData.get().getStock()) {
                    comment.getAndUpdate(s -> s + bitrixProductRows.getProductName() + "\n");
                }
            } else {
                comment.getAndUpdate(s -> s + bitrixProductRows.getProductName() + "\n");
            }
        });

        String stage = StageEnum.VALIDACION_PAGO_CUPO.getValue();
        if (comment.compareAndSet("Productos sin existencia:\n", "")) {
            stage = StageEnum.SEGUIMIENTO_DEL_PEDIDO.getValue();
            DocumentPed documentPed = documentPedRepository.save(DocumentPed.builder()
                    .numero(new Random().nextInt(999999 - 1 + 1) + 1)
                    .bodega(1)
                    .nit(Long.parseLong(dead.getNitPay()))
                    .valor_total(dead.getAmount())
                    .fecha(LocalDateTime.now())
                    .build());

            SoftJSDocumentPed softJSDocumentPed = softJSDocumentPedRepository.save(SoftJSDocumentPed.builder()
                    .idDocPed(documentPed.getId())
                    .fecha(documentPed.getFecha())
                    .bodega(documentPed.getBodega())
                    .valor_total(documentPed.getValor_total())
                    .nit(documentPed.getNit())
                    .numero(documentPed.getNumero())
                    .build());
        }

        bitrixUtils.updateDeal(BitrixUpdate.builder()
                .id(String.valueOf(dead.getId()))
                .fields(BitrixDead.builder()
                        .contactId(dead.getContactId())
                        .companyId(dead.getCompanyId())
                        .stageId(stage)
                        .amount(dead.getAmount())
                        .comments(comment.get())
                        .nitPay(dead.getNitPay())
                        .build())
                .build());
    }

    public void paymentValidation(Payment model) {
        BitrixResult<List<BitrixDead>> bitrixResult = bitrixUtils.getDeadByField(BitrixGetList.builder()
                        .filter(Map.of("UF_CRM_1741043732565", model.getNit(),
                                "STAGE_ID", StageEnum.VALIDACION_PAGO_CUPO.getValue()))
                        .build())
                .getBody();

        if (bitrixResult != null && bitrixResult.getResult().size() == 1) {
            BitrixDead dead = bitrixResult.getResult().getFirst();
            bitrixUtils.updateDeal(BitrixUpdate.builder()
                    .id(String.valueOf(dead.getId()))
                    .fields(BitrixDead.builder()
                            .contactId(dead.getContactId())
                            .companyId(dead.getCompanyId())
                            .stageId(StageEnum.PEDIDO.getValue())
                            .amount(dead.getAmount())
                            .comments("Pago Validado")
                            .nitPay(dead.getNitPay())
                            .build())
                    .build());
        } else {
            log.error("");
        }
    }

    public void createOrUpdateProduct(Product product) {
        BitrixResult<List<BitrixProduct>> responseProduct = bitrixUtils.getProductByField(BitrixGetList.builder()
                        .filter(Map.of("NAME", product.getCodigo()))
                        .build())
                .getBody();

        AtomicReference<String> bodega = new AtomicReference<>("");
        AtomicLong stock = new AtomicLong();
        LocalDate date = LocalDate.now();
        List<StockData> stockList = stockRepository.findByCodigoAndAnoAndMes(product.getCodigo(), date.getYear(), date.getMonthValue());
        stockList.forEach(stockData -> {
            bodega.getAndAccumulate(stockData.getBodega() + ". S:" + stockData.getStock() + " ", String::concat);
            stock.getAndAdd(stockData.getStock());
        });
        if (responseProduct != null && !responseProduct.getResult().isEmpty()) {
            BitrixResult<String> response = bitrixUtils.updateProduct(
                            BitrixUpdate.<BitrixProductCreation>builder()
                                    .id(responseProduct.getResult().getFirst().getId())
                                    .fields(BitrixProductCreation.builder()
                                            .description(product.getDescripcion())
                                            .detailText(product.getDescripcion())
                                            .previewText(product.getDescripcion())
                                            .price(product.getValor_unitario())
                                            .bodega(bodega.get())
                                            .stock(stock.get())
                                            .group(product.getGrupo())
                                            .subgroup(product.getSubgrupo())
                                            .subgroup2(product.getSubgrupo2())
                                            .subgroup3(product.getSubgrupo3())
                                            .manageOtherUnit(product.getManeja_otra_und())
                                            .otherUnit(product.getOtra_und())
                                            .build())
                                    .build())
                    .getBody();
            log.info("Product updated: {}", response != null ? response.getResult() : "");
        } else {
            BitrixResult<Map<String, Object>> response = bitrixUtils.addProduct(
                            BitrixAdd.<BitrixProductCreation>builder()
                                    .fields(BitrixProductCreation.builder()
                                            .iblockId(14)
                                            .name(product.getCodigo())
                                            .code(product.getCodigo())
                                            .active('Y')
                                            .currency("COP")
                                            .description(product.getDescripcion())
                                            .detailText(product.getDescripcion())
                                            .previewText(product.getDescripcion())
                                            .bodega(bodega.get())
                                            .stock(stock.get())
                                            .price(product.getValor_unitario())
                                            .group(product.getGrupo())
                                            .subgroup(product.getSubgrupo())
                                            .subgroup2(product.getSubgrupo2())
                                            .subgroup3(product.getSubgrupo3())
                                            .manageOtherUnit(product.getManeja_otra_und())
                                            .otherUnit(product.getOtra_und())
                                            .build())
                                    .build())
                    .getBody();
            log.info("Product created: {}", response != null ? response.getResult() : "");
        }
    }

    public void createOrUpdateCompany(Company company) {
        if (filterCompany(company)) {
            BitrixResult<List<BitrixCustomFields>> result = bitrixUtils.getCompanyCustomFields(BitrixGetList.builder()
                            .build())
                    .getBody();

            if (result != null) {
                Map<String, Map<String, Integer>> fieldsValues = result.getResult()
                        .stream()
                        .filter(bitrixCustomFields -> bitrixCustomFields.getValues() != null)
                        .collect(Collectors.toMap(
                                BitrixCustomFields::getFieldName,
                                listItem -> listItem.getValues().stream().collect(Collectors.toMap(
                                        BitrixCustomFields.ListItem::getValue,
                                        BitrixCustomFields.ListItem::getId)
                                )));

                BitrixCompany bitrixCompany = BitrixCompany.builder()
                        .nit(company.getNit())
                        .identificationNumber(company.getDigito())
                        .title(company.getNombres())
                        .build();

                List<BitrixData> phones = new ArrayList<>();

                if (company.getTelefono_1() != null)
                    phones.add(BitrixData.builder()
                            .valueType("WORK")
                            .value(company.getTelefono_1())
                            .build());

                if (company.getTelefono_2() != null)
                    phones.add(BitrixData.builder()
                            .valueType("WORK")
                            .value(company.getTelefono_1())
                            .build());

                if (company.getCelular() != null)
                    phones.add(BitrixData.builder()
                            .valueType("MOBILE")
                            .value(company.getTelefono_1())
                            .build());

                if (!phones.isEmpty())
                    bitrixCompany.setPhone(phones);

                if (company.getTipo_identificacion() != null)
                    fieldsValues.get("UF_CRM_1735235679")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().startsWith(company.getTipo_identificacion()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setDocumentType(stringIntegerEntry.getValue()));

                if (company.getY_pais() != null)
                    fieldsValues.get("UF_CRM_1735235544")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().equals(company.getY_pais()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setCountry(stringIntegerEntry.getValue()));

                if (company.getY_dpto() != null)
                    fieldsValues.get("UF_CRM_1735235642")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().equals(company.getY_dpto()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setDepartment(stringIntegerEntry.getValue()));

                if (company.getY_ciudad() != null)
                    fieldsValues.get("UF_CRM_1735235586")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().equals(company.getY_ciudad()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setCity(stringIntegerEntry.getValue()));

                if (company.getBloqueo() != null)
                    fieldsValues.get("UF_CRM_1735235709")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().equals(company.getBloqueo()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setState(stringIntegerEntry.getValue()));

                if (company.getId_definicion_tributaria_tipo() != null)
                    fieldsValues.get("UF_CRM_1735235737")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().equals(company.getId_definicion_tributaria_tipo()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setTaxProfile(stringIntegerEntry.getValue()));

                if (company.getRegimen() != null)
                    fieldsValues.get("UF_CRM_1735235765")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().equals(company.getRegimen()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setRegimen(stringIntegerEntry.getValue()));

                if (company.getConcepto_14() != null)
                    fieldsValues.get("UF_CRM_1735235787")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().equals(company.getConcepto_14()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setClientCategory(stringIntegerEntry.getValue()));

                if (company.getCondicion() != null)
                    fieldsValues.get("UF_CRM_1735235813")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().equals(company.getCondicion()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setPayCondition(stringIntegerEntry.getValue()));

                if (company.getGran_contribuyente() != null)
                    fieldsValues.get("UF_CRM_1735235846")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().equals(company.getGran_contribuyente()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setMajorContributor(stringIntegerEntry.getValue()));

                if (company.getAutoretenedor() != null)
                    fieldsValues.get("UF_CRM_1735235870")
                            .entrySet()
                            .stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getKey().equals(company.getAutoretenedor()))
                            .findFirst()
                            .ifPresent(stringIntegerEntry -> bitrixCompany.setAutoRetainer(stringIntegerEntry.getValue()));

                BitrixResult<List<BitrixCompany>> response = bitrixUtils.getCompanyByField(BitrixGetList.builder()
                                .filter(Map.of("UF_CRM_1735236034434", company.getNit()))
                                .build())
                        .getBody();

                if (response != null && !response.getResult().isEmpty()) {
                    BitrixResult<String> bitrixResult = bitrixUtils.updateCompany(BitrixUpdate
                                    .<BitrixCompany>builder()
                                    .id(response.getResult().getFirst().getId())
                                    .fields(bitrixCompany)
                                    .build())
                            .getBody();
                    log.info("Company updated: {}", bitrixResult != null ? bitrixResult.getResult() : "");
                } else {
                    BitrixResult<Integer> bitrixResult = bitrixUtils.addCompany(BitrixAdd
                                    .<BitrixCompany>builder()
                                    .fields(bitrixCompany)
                                    .build())
                            .getBody();
                    log.info("Company created: {}", bitrixResult != null ? bitrixResult.getResult() : "");
                }
            }
        }
    }

    public void createOrUpdateContact(Contact contact) {
        if (filterContact(contact)) {
            BitrixContact bitrixContact = BitrixContact.builder()
                    .name(contact.getNombre())
                    .lastName(contact.getApellidos())
                    .build();

            try {
                bitrixContact.setNit(Long.parseLong(contact.getNit()));
            } catch (NumberFormatException ignored) {
            }

            try {
                bitrixContact.setContact(Long.parseLong(contact.getContacto()));
            } catch (NumberFormatException ignored) {
            }

            try {
                bitrixContact.setExtension(Long.parseLong(contact.getExt1()));
            } catch (NumberFormatException ignored) {
            }

            if (contact.getE_mail() != null)
                bitrixContact.setEmail(List.of(BitrixData.builder()
                        .valueType("WORK")
                        .value(contact.getE_mail())
                        .build()));
            if (contact.getTel_ofi1() != null)
                bitrixContact.setPhone(List.of(BitrixData.builder()
                        .valueType("WORK")
                        .value(contact.getTel_ofi1())
                        .build()));

            BitrixResult<List<BitrixContact>> response = bitrixUtils.getContactByField(BitrixGetList.builder()
                            .filter(Map.of(
                                    "UF_CRM_1735240890449", contact.getContacto(),
                                    "UF_CRM_1735240902014", contact.getNit()
                            ))
                            .build())
                    .getBody();

            if (response != null && !response.getResult().isEmpty()) {
                BitrixResult<String> result = bitrixUtils.updateContact(BitrixUpdate
                                .<BitrixContact>builder()
                                .id(response.getResult().getFirst().getId())
                                .fields(bitrixContact)
                                .build())
                        .getBody();
                log.info("Contact updated: {}", result != null ? result.getResult() : "");
            } else {
                BitrixResult<Integer> result = bitrixUtils.addContact(BitrixAdd.builder()
                                .fields(bitrixContact)
                                .build())
                        .getBody();
                log.info("Contact created: {}", result != null ? result.getResult() : "");
            }
        }
    }

    private boolean filterContact(Contact contact) {
        if (contact.getNombre().matches("(?i:\\(.*(no|fallecio|fallecido|retiro|pensionad([ao])).*)|(?i:(falleci))|^([0.])$"))
            return false;
        else if (contact.getTel_ofi1() == null && contact.getTel_ofi2() == null && contact.getTel_celular() == null)
            return false;
        else
            return !contact.getE_mail().matches(".*(@une\\.net\\.co)$");
    }

    private boolean filterCompany(Company company) {
        return !String.valueOf(company.getNit()).matches("^444");
    }

    private BitrixResult<BitrixDead> getDead(long id) throws BitrixException {
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

    private BitrixResult<BitrixProduct> getProduct(long id) throws BitrixException {
        try {
            return bitrixUtils.getProduct(id).getBody();
        } catch (HttpClientErrorException e) {
            BitrixError error = e.getResponseBodyAs(BitrixError.class);
            throw new BitrixException(error.getErrorDescription());
        }
    }

    private BitrixResult<BitrixContact> getContact(long id) throws BitrixException {
        try {
            return bitrixUtils.getContact(id).getBody();
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
}
