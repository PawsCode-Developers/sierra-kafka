package com.pawcodes.sierra.kafka.bitrix.model.bitrix;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BitrixEvent {
    private String event;
    private String event_handler_id;
    private Map<String, Map<String, String>> data;
    private String ts;
    private Map<String, String> auth;
}
