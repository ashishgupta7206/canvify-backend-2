package com.canvify.test.repository;

import com.canvify.test.entity.Orders;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    List<Orders> findByUserIdAndBitDeletedFlagFalse(Long userId);

    Optional<Orders> findByIdAndBitDeletedFlagFalse(Long Id);

    List<Orders> findByStatusAndBitDeletedFlagFalse(OrderStatus status);

    @Query("""
    SELECT o FROM Orders o
    WHERE o.paymentStatus = :paymentStatus
      AND o.status = :status
      AND o.orderDate <= :expiryTime
      AND o.bitDeletedFlag = false
""")
    List<Orders> findExpiredOrders(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("status") OrderStatus status,
            @Param("expiryTime") LocalDateTime expiryTime
    );

}