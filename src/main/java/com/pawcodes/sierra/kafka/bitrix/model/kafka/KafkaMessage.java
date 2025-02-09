package com.pawcodes.sierra.kafka.bitrix.model.kafka;

import lombok.Data;

@Data
public class KafkaMessage<T> {

    Payload<T> payload;
}
