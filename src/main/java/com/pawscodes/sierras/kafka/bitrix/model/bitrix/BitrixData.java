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
public class BitrixData {
    @JsonProperty("ID")
    int id;

    @JsonProperty("VALUE_TYPE")
    String valueType;

    @JsonProperty("VALUE")
    String value;

    @JsonProperty("TYPE_ID")
    String type;
}
