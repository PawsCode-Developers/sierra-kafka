package com.pawscodes.sierras.kafka.bitrix.util;

import com.pawscodes.sierras.kafka.bitrix.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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

    public void createOrUpdateProduct(String id) {
        callBitrixMigrator("/migrate/products", List.of(id))
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public void createOrUpdateCompany(long id) {
        callBitrixMigrator("/migrate/companies", List.of(id))
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public void createOrUpdateContact(Customer customer) {
        callBitrixMigrator("/migrate/customers", List.of(customer))
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    private <T> RestClient.ResponseSpec callBitrixMigrator(String method, T body) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("x-service-id", "Java Integration");
        return restClientUtil
                .createRequest(
                        UriComponentsBuilder
                                .fromUriString(migrationAppUrl)
                                .path(method)
                                .toUriString(),
                        MediaType.APPLICATION_JSON,
                        HttpMethod.POST,
                        httpHeaders,
                        body)
                .retrieve();
    }
}
