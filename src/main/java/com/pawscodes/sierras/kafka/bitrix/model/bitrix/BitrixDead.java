package com.pawscodes.sierras.kafka.bitrix.model.bitrix;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitrixDead {

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

    @JsonProperty("BEGINDATE")
    OffsetDateTime beginDate;

    @JsonProperty("COMPANY_TITLE")
    String companyName;

    @JsonProperty("COMMENTS")
    String comments;

    @JsonProperty(value = "DATE_MODIFY", access = JsonProperty.Access.WRITE_ONLY)
    OffsetDateTime modifyDate;

    @JsonProperty("ASSIGNED_BY_ID")
    long assigned;

    @JsonProperty("UF_CRM_1741043732565")
    String nitPay;
}
