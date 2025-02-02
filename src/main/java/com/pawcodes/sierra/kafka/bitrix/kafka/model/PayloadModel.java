package com.pawcodes.sierra.kafka.bitrix.kafka.model;

import lombok.Data;

@Data
public class PayloadModel<T> {
    T after;
}
