package com.canvify.test.request.delivery;

import com.canvify.test.enums.DeliveryAssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAssignmentStatusRequest {
    @NotNull
    private DeliveryAssignmentStatus status;
    private String remark;
}