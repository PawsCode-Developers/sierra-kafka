package com.pawscodes.sierras.kafka.bitrix.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;

@Data
@Builder
public class Context<T, K> {
    private HttpHeaders headers;
    private String event;
    private T request;
    private K response;
}
