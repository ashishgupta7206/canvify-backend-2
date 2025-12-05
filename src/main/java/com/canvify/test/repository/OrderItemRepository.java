package com.canvify.test.repository;

import com.canvify.test.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdAndBitDeletedFlagFalse(Long orderId);
}