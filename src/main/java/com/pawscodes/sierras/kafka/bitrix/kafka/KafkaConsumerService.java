package com.pawscodes.sierras.kafka.bitrix.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pawscodes.sierras.kafka.bitrix.exception.BitrixException;
import com.pawscodes.sierras.kafka.bitrix.gateway.Gateway;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.Payload;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Company;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Contact;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Payment;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.Product;
import com.pawscodes.sierras.kafka.bitrix.util.MappingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableKafka
public class KafkaConsumerService {

    private final MappingUtil mappingUtil;
    private final Gateway gateway;

    public KafkaConsumerService(MappingUtil mappingUtil, Gateway gateway) {
        this.mappingUtil = mappingUtil;
        this.gateway = gateway;
    }

    @KafkaListener(topics = "master.PRUEBAS.dbo.referencias")
    public void consumerProducts(String message) {
        Payload<Product> model = mappingUtil.convertToType(message, new TypeReference<>() {
        });
        log.info("Received message referencias: {}", model);
        gateway.createOrUpdateProduct(model.getAfter());
    }

    @KafkaListener(topics = "master.PRUEBAS.dbo.terceros")
    public void consumerCompany(String message) {
        Payload<Company> model = mappingUtil.convertToType(message, new TypeReference<>() {
        });
        log.info("Received message terceros: {}", model);
        gateway.createOrUpdateCompany(model.getAfter());
    }

    @KafkaListener(topics = "master.PRUEBAS.dbo.CRM_contactos")
    public void consumerContact(String message) {
        Payload<Contact> model = mappingUtil.convertToType(message, new TypeReference<>() {
        });
        log.debug("Received message CRM_contactos: {}", model);
        gateway.createOrUpdateContact(model.getAfter());
    }

    @KafkaListener(topics = "master.PRUEBAS.dbo.sistema_autorizacion_13")
    public void consumePayment(String message) throws BitrixException {
        Payload<Payment> model = mappingUtil.convertToType(message, new TypeReference<>() {
        });
        log.debug("Received message sistema_autorizacion_13: {}", model);
        gateway.paymentValidation(model.getAfter());
    }
}
