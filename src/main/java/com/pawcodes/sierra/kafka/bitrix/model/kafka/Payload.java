package com.pawcodes.sierra.kafka.bitrix.model.kafka;

import lombok.Data;

@Data
public class Payload<T> {
    T before;
    T after;
}
