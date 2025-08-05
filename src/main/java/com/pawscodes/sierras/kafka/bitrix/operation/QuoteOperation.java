package com.pawscodes.sierras.kafka.bitrix.operation;

import com.pawscodes.sierras.kafka.bitrix.exception.SecondUnitException;
import com.pawscodes.sierras.kafka.bitrix.gateway.Gateway;
import com.pawscodes.sierras.kafka.bitrix.model.ConsumerRequest;
import com.pawscodes.sierras.kafka.bitrix.model.Context;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
public class QuoteOperation {

    private final Gateway gateway;

    public QuoteOperation(Gateway gateway) {
        this.gateway = gateway;
    }

    public void handle(ConsumerRequest<String> consumerRequest) {
        ((Function<ConsumerRequest<String>, Context<Long, String>>) this::getQueryParams)
                .andThen(this::processRequest)
                .andThen(this::prepareResponse)
                .apply(consumerRequest);
    }

    private Context<Long, String> getQueryParams(ConsumerRequest<String> consumerRequest) {
        return Context.<Long, String>builder()
                .headers(consumerRequest.getHeaders())
                .event(consumerRequest.getQueryParam("event").toString())
                .request(Long.valueOf(consumerRequest.getRequest()))
                .build();
    }

    @SneakyThrows
    private Context<Long, String> processRequest(Context<Long, String> context) {
        try {
            gateway.process(context.getRequest());
        } catch (SecondUnitException ex) {
            gateway.unexpectedError(ex.getDeal(), ex.getMessage());
        }
        return context;
    }

    private ResponseEntity<?> prepareResponse(Context<Long, String> context) {
        return new ResponseEntity<>(HttpEntity.EMPTY, HttpStatus.OK);
    }
}
