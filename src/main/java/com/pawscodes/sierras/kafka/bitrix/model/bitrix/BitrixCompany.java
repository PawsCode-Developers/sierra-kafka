package com.pawscodes.sierras.kafka.bitrix.model.bitrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pawscodes.sierras.kafka.bitrix.config.BitrixIntegerDeserializer;
import com.pawscodes.sierras.kafka.bitrix.config.BitrixLongDeserializer;
import com.pawscodes.sierras.kafka.bitrix.config.BitrixStringDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitrixCompany {
    @JsonProperty("ID")
    String id;

    @JsonProperty("COMPANY_TYPE")
    String companyType;

    @JsonProperty("TITLE")
    String title;

    @JsonProperty("CURRENCY_ID")
    String currencyId;

    @JsonProperty("EMPLOYEES")
    String employees;

    @JsonProperty("ADDRESS")
    String address;

    @JsonProperty("ADDRESS_CITY")
    String addressCity;

    @JsonProperty("ADDRESS_POSTAL_CODE")
    String addressPostalCode;

    @JsonProperty("ADDRESS_REGION")
    String addressRegion;

    @JsonProperty("ADDRESS_PROVINCE")
    String addressProvince;

    @JsonProperty("ADDRESS_COUNTRY")
    String addressCountry;

    @JsonProperty("ADDRESS_COUNTRY_CODE")
    String addressCountryCode;

    @JsonProperty("COMMENTS")
    String comments;

    @JsonProperty("PHONE")
    List<BitrixData> phone;

    @JsonProperty("EMAIL")
    List<BitrixData> email;

    @JsonDeserialize(using = BitrixLongDeserializer.class)
    @JsonProperty("UF_CRM_1735236034434")
    long nit;

    @JsonDeserialize(using = BitrixStringDeserializer.class)
    @JsonProperty("UF_CRM_1735236045783")
    String identificationNumber;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235679")
    int documentType;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235544")
    int country;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235586")
    int city;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235642")
    int department;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235709")
    int state;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235737")
    int taxProfile;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235765")
    int regimen;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235787")
    int clientCategory;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235813")
    int payCondition;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235846")
    int majorContributor;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1735235870")
    int autoRetainer;
}
