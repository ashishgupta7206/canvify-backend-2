package com.canvify.test.repository;

import com.canvify.test.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderIdAndBitDeletedFlagFalse(Long orderId);
    Optional<Payment> findByProviderPaymentIdAndBitDeletedFlagFalse(String providerPaymentId);
    Optional<Payment> findByProviderOrderIdAndBitDeletedFlagFalse(String ProviderOrderId);

}