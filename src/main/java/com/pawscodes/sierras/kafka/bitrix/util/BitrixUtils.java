package com.pawscodes.sierras.kafka.bitrix.util;

import com.pawscodes.sierras.kafka.bitrix.model.bitrix.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
public class BitrixUtils {

    @Value("${rest.bitrix.url}")
    String bitrixUrl;

    private final RestClientUtil restClientUtil;

    public BitrixUtils(RestClientUtil restClientUtil) {
        this.restClientUtil = restClientUtil;
    }

    public ResponseEntity<BitrixResult<BitrixLeadBase>> getLead(long id) {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        valueMap.add("id", String.valueOf(id));

        return callBitrixGet("crm.deal.get", valueMap)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public ResponseEntity<BitrixResult<BitrixCompany>> getCompany(long id) {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        valueMap.add("id", String.valueOf(id));

        return callBitrixGet("crm.company.get", valueMap)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<List<BitrixCustomFields>>> getCompanyCustomFields(BitrixGetList<T> filters) {
        return callBitrixPost("crm.company.userfield.list", filters)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<List<BitrixCompany>>> getCompanyByField(BitrixGetList<T> filters) {
        return callBitrixPost("crm.company.list", filters)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<Integer>> addCompany(T company) {
        return callBitrixPost("crm.company.add", company)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<String>> updateCompany(T company) {
        return callBitrixPost("crm.company.update", company)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public ResponseEntity<BitrixResult<BitrixContact>> getContact(long id) {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        valueMap.add("id", String.valueOf(id));

        return callBitrixGet("crm.contact.get", valueMap)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<List<BitrixContact>>> getContactByField(BitrixGetList<T> filters) {
        return callBitrixPost("crm.contact.list", filters)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<Integer>> addContact(T contact) {
        return callBitrixPost("crm.contact.add", contact)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<String>> updateContact(T contact) {
        return callBitrixPost("crm.contact.update", contact)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public ResponseEntity<BitrixResult<BitrixContact>> getProduct(long id) {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        valueMap.add("id", String.valueOf(id));

        return callBitrixGet("crm.product.get", valueMap)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<String>> addProduct(T product) {
        return callBitrixPost("crm.product.add", product)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<String>> updateProduct(T product) {
        return callBitrixPost("crm.product.update", product)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    private RestClient.ResponseSpec callBitrixGet(String method, MultiValueMap<String, String> map) {
        return restClientUtil
                .createRequest(
                        UriComponentsBuilder
                                .fromUriString(bitrixUrl)
                                .pathSegment(method)
                                .queryParams(map)
                                .toUriString(),
                        MediaType.APPLICATION_JSON,
                        HttpMethod.GET,
                        new HttpHeaders(),
                        "")
                .retrieve();
    }

    private <T> RestClient.ResponseSpec callBitrixPost(String method, T body) {
        return restClientUtil
                .createRequest(
                        UriComponentsBuilder
                                .fromUriString(bitrixUrl)
                                .pathSegment(method)
                                .toUriString(),
                        MediaType.APPLICATION_JSON,
                        HttpMethod.POST,
                        new HttpHeaders(),
                        body)
                .retrieve();
    }
}
