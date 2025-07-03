package com.pawscodes.sierras.kafka.bitrix.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pawscodes.sierras.kafka.bitrix.exception.BitrixException;
import com.pawscodes.sierras.kafka.bitrix.gateway.Gateway;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.PayloadKafka;
import com.pawscodes.sierras.kafka.bitrix.model.kafka.table.*;
import com.pawscodes.sierras.kafka.bitrix.util.MappingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
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

    @KafkaListener(topics = "master.SIERRAS.dbo.referencias")
    public void consumerProducts(@Payload(required = false) String message) {
        if (message != null && !message.isEmpty()) {
            PayloadKafka<Product> model = mappingUtil.convertToType(message, new TypeReference<>() {
            });
            log.debug("Received message referencias: {}", model);
            gateway.createOrUpdateProduct(model.getAfter());
        } else
            log.debug("Product: {}", message);
    }

    @KafkaListener(topics = "master.SIERRAS.dbo.terceros")
    public void consumerCompany(@Payload(required = false) String message) {
        if (message != null && !message.isEmpty()) {
            PayloadKafka<Company> model = mappingUtil.convertToType(message, new TypeReference<>() {
            });
            log.debug("Received message terceros: {}", model);
            gateway.createOrUpdateCompany(model.getAfter());
        } else
            log.debug("Company: {}", message);
    }

    @KafkaListener(topics = "master.SIERRAS.dbo.CRM_contactos")
    public void consumerContact(@Payload(required = false) String message) {
        if (message != null && !message.isEmpty()) {
            PayloadKafka<Contact> model = mappingUtil.convertToType(message, new TypeReference<>() {
            });
            log.debug("Received message CRM_contactos: {}", model);
            gateway.createOrUpdateContact(model.getAfter());
        } else
            log.debug("Contact: {}", message);
    }

    @KafkaListener(topics = "master.SIERRAS.dbo.sistema_autorizacion_13")
    public void consumePayment(@Payload(required = false) String message) throws BitrixException {
        if (message != null && !message.isEmpty()) {
            PayloadKafka<Payment> model = mappingUtil.convertToType(message, new TypeReference<>() {
            });
            log.debug("Received message sistema_autorizacion_13: {}", model);
            gateway.paymentValidation(model.getAfter());
        } else
            log.debug("Payment: {}", message);
    }

    @KafkaListener(topics = "master.SIERRAS.dbo.softjs_prd_proceso_actividad_start_stop")
    public void consumePrdProcess(@Payload(required = false) String message) {
        if (message != null && !message.isEmpty()) {
            PayloadKafka<PrdProcess> model = mappingUtil.convertToType(message, new TypeReference<>() {
            });
            log.debug("Received message softjs_prd_proceso_actividad_start_stop: {}", model);
            gateway.updatePrdProcessStatus(model.getAfter());
        } else
            log.debug("PrdProcess: {}", message);
    }

    @KafkaListener(topics = "master.SIERRAS.dbo.documentos")
    public void consumeBill(@Payload(required = false) String message) {
        if (message != null && !message.isEmpty()) {
            PayloadKafka<Bill> model = mappingUtil.convertToType(message, new TypeReference<>() {
            });
            log.debug("Received message documentos: {}", model);
            gateway.updateBillStatus(model.getAfter());
        } else
            log.debug("Bill: {}", message);
    }
}
