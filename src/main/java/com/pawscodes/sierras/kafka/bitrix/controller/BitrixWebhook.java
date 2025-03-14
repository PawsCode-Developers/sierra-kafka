package com.pawscodes.sierras.kafka.bitrix.controller;

import com.pawscodes.sierras.kafka.bitrix.model.ConsumerRequest;
import com.pawscodes.sierras.kafka.bitrix.model.bitrix.BitrixEvent;
import com.pawscodes.sierras.kafka.bitrix.operation.QuoteOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class BitrixWebhook {

    private final QuoteOperation quoteOperation;

    public BitrixWebhook(QuoteOperation quoteOperation) {
        this.quoteOperation = quoteOperation;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void getEvent(@RequestHeader HttpHeaders headers, @ModelAttribute BitrixEvent body) {
        if (body.getAuth().get("application_token").equals("53sezbrrfmpg3oi4ajnwsdru8nmri0cg") &&
                body.getAuth().get("domain").equals("sierrasyequipos.bitrix24.co")) {

            quoteOperation.handle(ConsumerRequest
                    .<String>builder()
                    .headers(headers)
                    .queryParams(Map.of("event", body.getEvent()))
                    .request(body.getData().get("FIELDS").get("ID"))
                    .build());
        }
    }
}
