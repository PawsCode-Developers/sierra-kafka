package com.pawscodes.sierras.kafka.bitrix.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestClientUtil {
    private final RestClient restClient;
    private final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 2);

    public RestClientUtil() {
        this.restClient = RestClient.create();
    }

    public <T> RestClient.RequestBodySpec createRequest(String url, MediaType mediaType, HttpMethod httpMethod, HttpHeaders httpHeaders, T request) {
        try {
            limiter.waitIfNeeded();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait interrupted", e);
        }

        return restClient
                .method(httpMethod)
                .uri(url)
                .contentType(mediaType)
                .headers(headers -> headers.addAll(httpHeaders))
                .body(request);
    }
}
