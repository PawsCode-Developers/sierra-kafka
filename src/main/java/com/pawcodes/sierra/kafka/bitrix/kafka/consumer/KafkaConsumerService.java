package com.pawcodes.sierra.kafka.bitrix.kafka.consumer;

import com.pawcodes.sierra.kafka.bitrix.kafka.model.AfterModel;
import com.pawcodes.sierra.kafka.bitrix.kafka.model.DbMessageModel;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@EnableKafka
public class KafkaConsumerService {

    //@KafkaHandler
    @KafkaListener(topics = "master.Test_DB.dbo.prospects")
    public void consume(@Payload DbMessageModel<AfterModel> message) {
        System.out.println("Received message: " + message.toString());
    }
}
