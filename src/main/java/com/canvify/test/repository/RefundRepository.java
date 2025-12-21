package com.canvify.test.repository;

import com.canvify.test.entity.Refund;
import com.canvify.test.enums.PaymentStatus;
import com.canvify.test.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    boolean existsByPaymentIdAndStatus(Long paymentId, RefundStatus status);
}
