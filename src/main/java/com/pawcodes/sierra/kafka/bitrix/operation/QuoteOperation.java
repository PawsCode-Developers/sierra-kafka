package com.pawcodes.sierra.kafka.bitrix.operation;

import com.pawcodes.sierra.kafka.bitrix.gateway.Gateway;
import com.pawcodes.sierra.kafka.bitrix.model.ConsumerRequest;
import com.pawcodes.sierra.kafka.bitrix.model.Context;
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
                .request(Long.valueOf(consumerRequest.getRequest()))
                .build();
    }

    @SneakyThrows
    private Context<Long, String> processRequest(Context<Long, String> context) {
        gateway.process(context.getRequest());
        return context;
    }

    private ResponseEntity<?> prepareResponse(Context<Long, String> context) {
        return new ResponseEntity<>(HttpEntity.EMPTY, HttpStatus.OK);
    }
}
