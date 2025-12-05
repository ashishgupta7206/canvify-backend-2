package com.canvify.test.dto.payment;

import lombok.Data;
import java.util.Map;

@Data
public class ProviderWebhookDTO {
    private String event;
    private Map<String, Object> data;
}