package com.canvify.test.service.orderstatus;

import com.canvify.test.entity.OrderStatusHistory;
import com.canvify.test.model.ApiResponse;

import java.util.List;

public interface OrderStatusHistoryService {
    ApiResponse<?> addHistory(Long orderId, String oldStatus, String newStatus, String updatedBy, String remark);
    ApiResponse<List<OrderStatusHistory>> getHistoryForOrder(Long orderId);
}