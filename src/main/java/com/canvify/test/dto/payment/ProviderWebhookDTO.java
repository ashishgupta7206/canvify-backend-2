package com.canvify.test.dto.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.util.Map;

@Data
public class ProviderWebhookDTO {
    private String event;
    private Map<String, Object> data;

    public static ProviderWebhookDTO fromJson(String rawJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(rawJson, ProviderWebhookDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid webhook payload", e);
        }
    }

}