package com.pawscodes.sierras.kafka.bitrix.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestClientUtil {
    private final RestClient restClient;

    public RestClientUtil() {
        this.restClient = RestClient.create();
    }

    public <T> RestClient.RequestBodySpec createRequest(String url, MediaType mediaType, HttpMethod httpMethod, HttpHeaders httpHeaders, T request) {
        return restClient
                .method(httpMethod)
                .uri(url)
                .contentType(mediaType)
                .headers(headers -> headers.addAll(httpHeaders))
                .body(request);
    }
}
