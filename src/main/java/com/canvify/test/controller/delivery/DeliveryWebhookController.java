package com.canvify.test.controller.delivery;

import com.canvify.test.request.delivery.PartnerWebhookRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.delivery.DeliveryWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partner-webhook")
@RequiredArgsConstructor
public class DeliveryWebhookController {

    private final DeliveryWorkflowService workflowService;

    @PostMapping("/status")
    public ApiResponse<?> partnerStatus(@Valid @RequestBody PartnerWebhookRequest req) {
        return workflowService.handlePartnerWebhook(req);
    }
}