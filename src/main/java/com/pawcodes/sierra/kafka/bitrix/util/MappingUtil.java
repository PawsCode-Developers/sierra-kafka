package com.pawcodes.sierra.kafka.bitrix.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class MappingUtil {

    private final ObjectMapper objectMapper;

    public MappingUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public <T> String convertToJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T convertToType(String json, TypeReference<T> returnClass) {
        try {
            return objectMapper.readValue(json, returnClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
