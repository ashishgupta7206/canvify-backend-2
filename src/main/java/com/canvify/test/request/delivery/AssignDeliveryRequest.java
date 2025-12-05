package com.canvify.test.request.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDeliveryRequest {
    @NotNull
    private Long orderId;

    @NotNull
    private Long partnerId;

    // optional courier/tracking info when assigning
    private String courierName;
    private String trackingNumber;
}