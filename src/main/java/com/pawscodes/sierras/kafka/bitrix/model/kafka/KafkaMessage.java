package com.pawscodes.sierras.kafka.bitrix.model.kafka;

import lombok.Data;

@Data
public class KafkaMessage<T> {

    PayloadKafka<T> payloadKafka;
}
