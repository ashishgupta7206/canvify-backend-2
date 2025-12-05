package com.canvify.test.request.shipment;

import com.canvify.test.enums.DeliveryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateShipmentRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String courierName;

    @NotBlank
    private String trackingNumber;

    private DeliveryStatus status = DeliveryStatus.SHIPPED;
}