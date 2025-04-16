package com.pawscodes.sierras.kafka.bitrix.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
public class MigrationAppUtil {

    @Value("${rest.migration.url}")
    String migrationAppUrl;

    private final RestClientUtil restClientUtil;

    public MigrationAppUtil(RestClientUtil restClientUtil) {
        this.restClientUtil = restClientUtil;
    }

    public ResponseEntity<?> createOrUpdateProduct(String id) {
        return callBitrixMigrator("start/products", List.of(id))
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public ResponseEntity<?> createOrUpdateContact(long id) {
        return callBitrixMigrator("start/contacts", List.of(id))
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    private <T> RestClient.ResponseSpec callBitrixMigrator(String method, T body) {
        return restClientUtil
                .createRequest(
                        UriComponentsBuilder
                                .fromUriString(migrationAppUrl)
                                .pathSegment(method)
                                .toUriString(),
                        MediaType.APPLICATION_JSON,
                        HttpMethod.POST,
                        new HttpHeaders(),
                        body)
                .retrieve();
    }
}
