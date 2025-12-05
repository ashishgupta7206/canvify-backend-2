package com.canvify.test.request.shipment;

import com.canvify.test.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateShipmentStatusRequest {

    @NotNull
    private DeliveryStatus status;

    private String remark; // for order status history
}