package com.pawscodes.sierras.kafka.bitrix.model.bitrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
public class BitrixContact {

    @JsonProperty("ID")
    String id;

    @JsonProperty("COMMENTS")
    String comments;

    @JsonProperty("NAME")
    String name;

    @JsonProperty("SECOND_NAME")
    String secondName;

    @JsonProperty("LAST_NAME")
    String lastName;

    @Builder.Default
    @JsonProperty("TYPE_ID")
    String typeId = "CLIENT";

    @JsonProperty("COMPANY_ID")
    String companyId;

    @JsonProperty("BIRTHDATE")
    String birthdate;

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

    @JsonProperty("PHONE")
    List<BitrixData> phone;

    @JsonProperty("EMAIL")
    List<BitrixData> email;

    @Builder.Default
    @JsonProperty("SOURCE_DESCRIPTION")
    String originInformation = "Spring Integration";

    @JsonDeserialize(using = BitrixStringDeserializer.class)
    @JsonProperty("POST")
    String cargo;

    @JsonDeserialize(using = BitrixLongDeserializer.class)
    @JsonProperty("UF_CRM_1735240902014")
    long nit;

    @JsonDeserialize(using = BitrixLongDeserializer.class)
    @JsonProperty("UF_CRM_1735240890449")
    long contact;

    @JsonDeserialize(using = BitrixLongDeserializer.class)
    @JsonProperty("UF_CRM_1735240932113")
    long extension;
}
