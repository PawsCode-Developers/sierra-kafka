package com.pawscodes.sierras.kafka.bitrix.model.bitrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitrixProduct {

    @JsonProperty("NAME")
    String name;

    @JsonProperty("CODE")
    String code;

    @Builder.Default
    @JsonProperty("ACTIVE")
    char active = 'Y';

    @JsonProperty("DESCRIPTION")
    String description;

    @JsonProperty("MEASURE")
    int measure;

    @JsonProperty("PRICE")
    double price;

    @JsonProperty("iblockId")
    long iblockId;

    String detailText;


}
