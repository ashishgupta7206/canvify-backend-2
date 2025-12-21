package com.canvify.test.service.orderstatus;

import com.canvify.test.entity.Orders;
import com.canvify.test.enums.OrderStatus;

public interface OrderStatusManager {

    void changeStatus(Orders order, OrderStatus newStatus, String remark);

}

