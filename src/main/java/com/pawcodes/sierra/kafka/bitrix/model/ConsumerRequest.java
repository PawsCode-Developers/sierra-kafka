package com.pawcodes.sierra.kafka.bitrix.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerRequest<T> {
    private HttpHeaders headers;
    private T request;
    private Map<String, Object> queryParams;

    public Object getQueryParam(String key) {
        return queryParams.get(key);
    }
}
