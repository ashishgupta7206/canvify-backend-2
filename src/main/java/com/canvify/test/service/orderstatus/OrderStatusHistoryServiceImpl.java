package com.canvify.test.service.orderstatus;

import com.canvify.test.entity.OrderStatusHistory;
import com.canvify.test.entity.Orders;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.repository.OrderRepository;
import com.canvify.test.repository.OrderStatusHistoryRepository;
import com.canvify.test.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {

    private final OrderStatusHistoryRepository historyRepository;
    private final OrderRepository orderRepository;

    @Override
    public ApiResponse<?> addHistory(Long orderId, String oldStatus, String newStatus, String updatedBy, String remark) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrder(order);
        h.setOldStatus(OrderStatus.valueOf(oldStatus));
        h.setNewStatus(OrderStatus.valueOf(newStatus));
        h.setUpdatedBy(updatedBy);
        h.setRemark(remark);
        h.setUpdatedOn(LocalDateTime.now());
        historyRepository.save(h);

        return ApiResponse.success("History added");
    }

    @Override
    public ApiResponse<List<OrderStatusHistory>> getHistoryForOrder(Long orderId) {
        var list = historyRepository.findByOrderIdOrderByUpdatedOnDesc(orderId);
        return ApiResponse.success(list);
    }
}