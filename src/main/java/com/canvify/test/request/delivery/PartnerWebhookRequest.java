package com.canvify.test.request.delivery;

import com.canvify.test.enums.DeliveryAssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PartnerWebhookRequest {
    @NotNull
    private Long assignmentId;

    @NotNull
    private DeliveryAssignmentStatus status;

    private String trackingNumber;
    private String remark;
    private String partnerReference; // partner's internal id (optional)
}