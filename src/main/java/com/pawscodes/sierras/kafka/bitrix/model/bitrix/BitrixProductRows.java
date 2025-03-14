package com.pawscodes.sierras.kafka.bitrix.model.bitrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BitrixProductRows {

    @JsonProperty("PRODUCT_ID")
    Long productId;

    @JsonProperty("PRODUCT_NAME")
    String productName;

    @JsonProperty("PRICE")
    double price;

    @JsonProperty("DISCOUNT_RATE")
    String discountRate;

    @JsonProperty("QUANTITY")
    int quantity;
}
