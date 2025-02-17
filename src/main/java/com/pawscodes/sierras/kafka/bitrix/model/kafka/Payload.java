package com.pawscodes.sierras.kafka.bitrix.model.kafka;

import lombok.Data;

@Data
public class Payload<T> {
    T before;
    T after;
}
