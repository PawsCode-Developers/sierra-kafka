package com.pawscodes.sierras.kafka.bitrix.gateway;

import com.pawscodes.sierras.kafka.bitrix.exception.BitrixException;
import com.pawscodes.sierras.kafka.bitrix.model.bitrix.*;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Company;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Contact;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Product;
import com.pawscodes.sierras.kafka.bitrix.util.BitrixUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Gateway {

    private final BitrixUtils bitrixUtils;

    public Gateway(BitrixUtils bitrixUtils) {
        this.bitrixUtils = bitrixUtils;
    }

    public void process(Long request) throws BitrixException {
        BitrixResult<BitrixLeadBase> lead = getLead(request);
        BitrixResult<BitrixCompany> company = null;
        BitrixResult<BitrixContact> contact = null;

        if (lead.getResult().getCompanyId() != 0) {
            company = getCompany(lead.getResult().getCompanyId());
        }
        if (lead.getResult().getContactId() != 0) {
            contact = getContact(lead.getResult().getContactId());
        }

        System.out.println(lead.getResult());
        System.out.println(company.getResult());
        System.out.println(contact.getResult());
    }

    public void createOrUpdateProduct(Product product) {
        bitrixUtils.addProduct(BitrixProduct.builder()
                .iblockId(16)
                .code(product.getCodigo())
                .active('Y')
                .description(product.getDescripcion())
                .detailText(product.getDescripcion())
                .build());
    }

    public void createOrUpdateCompany(Company company) {
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

    public void createOrUpdateContact(Contact contact) {
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

    private BitrixResult<BitrixLeadBase> getLead(long id) throws BitrixException {
        try {
            return bitrixUtils.getLead(id).getBody();
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
