package com.canvify.test.service.orderstatus;

import com.canvify.test.entity.OrderStatusHistory;
import com.canvify.test.entity.Orders;
import com.canvify.test.enums.OrderEventType;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderStatusHistoryServiceImpl
        implements OrderStatusHistoryService {

    private final OrderStatusHistoryRepository historyRepo;

    @Override
    public void recordStatusChange(
            Orders order,
            OrderStatus oldStatus,
            OrderStatus newStatus,
            OrderEventType eventType,
            String remark
    ) {
        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrder(order);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setEventType(eventType);
        h.setRemark(remark);

        historyRepo.save(h);
    }

    @Override
    public List<OrderStatusHistory> getHistoryForOrder(Long orderId) {
        return historyRepo.findByOrderIdOrderByCreatedDateAsc(orderId);
    }
}
