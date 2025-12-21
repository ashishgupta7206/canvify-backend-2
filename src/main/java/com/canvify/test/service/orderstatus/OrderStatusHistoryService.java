package com.canvify.test.service.orderstatus;

import com.canvify.test.entity.OrderStatusHistory;
import com.canvify.test.entity.Orders;
import com.canvify.test.enums.OrderEventType;
import com.canvify.test.enums.OrderStatus;

import java.util.List;

public interface OrderStatusHistoryService {

    void recordStatusChange(
            Orders order,
            OrderStatus oldStatus,
            OrderStatus newStatus,
            OrderEventType eventType,
            String remark
    );

    List<OrderStatusHistory> getHistoryForOrder(Long orderId);
}
