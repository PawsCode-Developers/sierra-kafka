package com.pawscodes.sierras.kafka.bitrix.model.bitrix;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pawscodes.sierras.kafka.bitrix.config.BitrixIntegerDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitrixDeal {

    @JsonProperty("ID")
    int id;

    @JsonProperty("TITLE")
    String title;

    @JsonProperty("STAGE_ID")
    String stageId;

    @JsonProperty("CURRENCY_ID")
    String currency;

    @JsonProperty("OPPORTUNITY")
    double amount;

    @JsonProperty("CONTACT_ID")
    int contactId;

    @JsonProperty("COMPANY_ID")
    int companyId;

    @JsonProperty("COMPANY_TITLE")
    String companyName;

    @JsonProperty("COMMENTS")
    String comments;

    @JsonProperty(value = "DATE_MODIFY", access = JsonProperty.Access.WRITE_ONLY)
    OffsetDateTime modifyDate;

    @JsonProperty("ASSIGNED_BY_ID")
    long assigned;

    @JsonProperty("UF_CRM_1743216219")
    String errorMessage;

    @JsonProperty("UF_CRM_1743439978")
    String warehouse;

    @JsonProperty("UF_CRM_1735241718870")
    String validDay;

    @JsonProperty("UF_CRM_1743530021292")
    String NoOrder;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1743774849")
    int concept;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1743774680")
    int concept2;

    @JsonDeserialize(using = BitrixIntegerDeserializer.class)
    @JsonProperty("UF_CRM_1744648899")
    int freightType;

    @JsonProperty("UF_CRM_1748312075240")
    String productionDetails;

    @JsonProperty("UF_CRM_1748492579602")
    String discountDetails;

    @JsonProperty("UF_CRM_1748495225512")
    String addresses;

    @JsonProperty("UF_CRM_1744398496")
    String deliveryAddress;

    @JsonProperty("UF_CRM_1749584576936")
    String detailsOtherUnit;

    @JsonProperty("UF_CRM_1750194510700")
    String otherUnits;

    @JsonProperty("UF_CRM_1750204659")
    String clientState;
}
