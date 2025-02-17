package com.pawscodes.sierras.kafka.bitrix.model.bitrix;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BitrixCustomFields {
    @JsonProperty("ID")
    String id;

    @JsonProperty("ENTITY_ID")
    String entityId;

    @JsonProperty("FIELD_NAME")
    String fieldName;

    @JsonProperty("LIST")
    List<ListItem> values;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListItem {
        @JsonProperty("ID")
        int id;

        @JsonProperty("SORT")
        String sort;

        @JsonProperty("VALUE")
        String value;

        @JsonProperty("DEF")
        String def;

        @JsonProperty("XML_ID")
        String xml_id;

    }
}
