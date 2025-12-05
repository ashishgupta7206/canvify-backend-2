package com.canvify.test.dto.shipment;

import com.canvify.test.enums.DeliveryStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShipmentDTO {

    private Long id;
    private Long orderId;
    private String courierName;
    private String trackingNumber;

    private LocalDateTime shippedOn;
    private LocalDateTime deliveredOn;

    private DeliveryStatus deliveryStatus;
}