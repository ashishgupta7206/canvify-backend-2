package com.canvify.test.repository;

import com.canvify.test.entity.Orders;
import com.canvify.test.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    List<Orders> findByUserIdAndBitDeletedFlagFalse(Long userId);

    List<Orders> findByStatusAndBitDeletedFlagFalse(OrderStatus status);
}