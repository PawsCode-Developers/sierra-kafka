package com.pawscodes.sierras.kafka.bitrix.model.kafka;

import lombok.Data;

@Data
public class PayloadKafka<T> {
    T before;
    T after;
}
