package com.canvify.test.service.delivery;

import com.canvify.test.dto.delivery.DeliveryAssignmentDTO;
import com.canvify.test.request.delivery.AssignDeliveryRequest;
import com.canvify.test.request.delivery.UpdateAssignmentStatusRequest;
import com.canvify.test.model.ApiResponse;

import java.util.List;

public interface DeliveryAssignmentService {
    ApiResponse<?> assign(AssignDeliveryRequest req);
    ApiResponse<?> updateStatus(Long assignmentId, UpdateAssignmentStatusRequest req);
    ApiResponse<?> get(Long id);
    ApiResponse<?> listByOrder(Long orderId);
    ApiResponse<?> listByPartner(Long partnerId, int page, int size);
}