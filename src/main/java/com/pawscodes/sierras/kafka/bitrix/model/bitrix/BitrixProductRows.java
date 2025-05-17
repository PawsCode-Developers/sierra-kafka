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
public class BitrixProductRows {

    @JsonProperty("PRODUCT_ID")
    Long productId;

    @JsonProperty("PRODUCT_NAME")
    String productName;

    @JsonProperty("PRICE")
    double price;

    @JsonProperty("DISCOUNT_TYPE_ID")
    int discountTypeId;

    @JsonProperty("DISCOUNT_RATE")
    double discountRate;

    @JsonProperty("DISCOUNT_SUM")
    double discountSum;

    @JsonProperty("QUANTITY")
    int quantity;

    @JsonProperty("TAX_RATE")
    int tax;

    @JsonProperty("MEASURE_NAME")
    String measure;
}
