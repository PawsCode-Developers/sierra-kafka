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
public class BitrixProductCreation {
    @JsonProperty("ID")
    String id;
    String name;
    String code;
    String currency;
    @Builder.Default
    char active = 'Y';
    String description;
    int measure;
    long iblockId;
    String detailText;
    String previewText;

    @JsonProperty("property98")
    Double price;

    @JsonProperty("property100")
    String bodega;

    @JsonProperty("property102")
    String group;

    @JsonProperty("property104")
    String subgroup;

    @JsonProperty("property106")
    String subgroup2;

    @JsonProperty("property108")
    String subgroup3;

    @JsonProperty("property110")
    long stock;

    @JsonProperty("property112")
    String manageOtherUnit;

    @JsonProperty("property114")
    String otherUnit;
}
