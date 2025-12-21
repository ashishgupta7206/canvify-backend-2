package com.canvify.test.response.order;

import com.canvify.test.entity.Orders;
import com.canvify.test.enums.OrderEventType;
import com.canvify.test.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderStatusHistoryResponse {
    Long orderId;
    OrderStatus oldStatus;
    OrderStatus newStatus;
    OrderEventType eventType;
    String remark;
    LocalDateTime createdDate;
}
