package com.canvify.test.service.orderstatus;

import com.canvify.test.entity.OrderStatusHistory;
import com.canvify.test.entity.Orders;
import com.canvify.test.enums.OrderEventType;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.repository.OrderRepository;
import com.canvify.test.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderStatusManagerImpl implements OrderStatusManager {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    // -----------------------
    // ORDER STATE MACHINE
    // -----------------------
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.PLACED, Set.of(OrderStatus.CONFIRMED, OrderStatus.PAID, OrderStatus.CANCELLED), // ✅ add PAID
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
            OrderStatus.PAID, Set.of(OrderStatus.SHIPPED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED)
    );

    @Override
    @Transactional
    public void changeStatus(Orders order, OrderStatus newStatus, String remark) {

        OrderStatus oldStatus = order.getStatus();

        if (oldStatus == newStatus) {
            return;
        }

        validateTransition(oldStatus, newStatus);

        order.setStatus(newStatus);
        orderRepository.save(order);

        // -----------------------
        // AUTO-LOG HISTORY
        // -----------------------
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setEventType(OrderEventType.SYSTEM);
        history.setRemark(remark);
        history.setLastModifiedDate(LocalDateTime.now());

        historyRepository.save(history);
    }

    private void validateTransition(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> allowed = TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new IllegalStateException(
                    "Invalid order status transition: " + from + " → " + to
            );
        }
    }
}

