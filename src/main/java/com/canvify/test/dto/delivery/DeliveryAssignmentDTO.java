package com.canvify.test.dto.delivery;

import com.canvify.test.enums.DeliveryAssignmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryAssignmentDTO {
    private Long id;
    private Long orderId;
    private Long partnerId;
    private String partnerName;
    private DeliveryAssignmentStatus status;
    private LocalDateTime assignedOn;
    private LocalDateTime deliveredOn;
}