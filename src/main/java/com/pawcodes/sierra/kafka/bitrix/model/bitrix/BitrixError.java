package com.pawcodes.sierra.kafka.bitrix.model.bitrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BitrixError {
    String error;

    @JsonProperty("error_description")
    String errorDescription;
}
