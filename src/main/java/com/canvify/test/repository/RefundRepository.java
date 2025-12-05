package com.canvify.test.repository;

import com.canvify.test.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByPaymentIdAndBitDeletedFlagFalse(Long paymentId);
    List<Refund> findByReturnRequestIdAndBitDeletedFlagFalse(Long returnId);
}