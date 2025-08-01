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
import java.util.Map;

@Slf4j
@Component
public class BitrixUtils {

    @Value("${rest.bitrix.url}")
    String bitrixUrl;

    private final RestClientUtil restClientUtil;

    public BitrixUtils(RestClientUtil restClientUtil) {
        this.restClientUtil = restClientUtil;
    }

    public ResponseEntity<BitrixResult<BitrixDeal>> getDeal(long id) {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        valueMap.add("id", String.valueOf(id));

        return callBitrixGet("crm.deal.get", valueMap)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<List<BitrixDeal>>> getDealByField(BitrixGetList<T> filters) {
        return callBitrixPost("crm.deal.list", filters)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public ResponseEntity<BitrixResult<List<BitrixProductRows>>> getDealProducts(long id) {
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap<>();
        valueMap.add("id", String.valueOf(id));

        return callBitrixGet("crm.deal.productrows.get", valueMap)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<String>> updateDealProduct(T deal) {
        return callBitrixPost("crm.deal.productrows.set", deal)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> void updateDeal(T deal) {
        callBitrixPost("crm.deal.update", deal)
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

    public <T> ResponseEntity<BitrixResult<List<BitrixCustomFields>>> getDealCustomFields(BitrixGetList<T> filters) {
        return callBitrixPost("crm.deal.userfield.list", filters)
                .toEntity(new ParameterizedTypeReference<>() {
                });
    }

    public <T> ResponseEntity<BitrixResult<List<BitrixUser>>> getUser(BitrixGetList<T> filters) {
        return callBitrixPost("user.get", filters)
                .toEntity(new ParameterizedTypeReference<>() {
                });

    }

    public <T> ResponseEntity<BitrixResult<Map<String, List<BitrixGetProduct>>>> getProductByFilter(BitrixGetList<T> filters) {
        return callBitrixPost("catalog.product.list", filters)
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
