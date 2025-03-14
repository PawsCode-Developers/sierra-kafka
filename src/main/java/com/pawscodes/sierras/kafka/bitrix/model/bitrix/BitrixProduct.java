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
    @JsonProperty("ID")
    String id;
    @JsonProperty("NAME")
    String name;
    @JsonProperty("CODE")
    String code;
    @JsonProperty("CURRENCY")
    String currency;
    @JsonProperty("DESCRIPTION")
    String description;
    @JsonProperty("MEASURE")
    int measure;

    @JsonProperty("PROPERTY_98")
    Property<Double> price;

    @JsonProperty("PROPERTY_100")
    Property<String> bodega;

    @JsonProperty("PROPERTY_102")
    Property<String> group;

    @JsonProperty("PROPERTY_104")
    Property<String> subgroup;

    @JsonProperty("PROPERTY_106")
    Property<String> subgroup2;

    @JsonProperty("PROPERTY_108")
    Property<String> subgroup3;

    @JsonProperty("PROPERTY_110")
    Property<Long> stock;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Property<T> {
        int valueId;
        T value;
    }
}
