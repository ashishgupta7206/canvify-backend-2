package com.canvify.test.service.delivery;

import com.canvify.test.request.delivery.PartnerWebhookRequest;
import com.canvify.test.model.ApiResponse;

public interface DeliveryWorkflowService {
    /**
     * Called by partner-system webhook to update assignment status
     * e.g., partner accepts pickup, picked up, in transit, delivered.
     */
    ApiResponse<?> handlePartnerWebhook(PartnerWebhookRequest req);
}