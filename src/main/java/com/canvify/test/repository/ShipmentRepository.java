package com.canvify.test.repository;

import com.canvify.test.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByOrderIdAndBitDeletedFlagFalse(Long orderId);

    Optional<Shipment> findByTrackingNumberAndBitDeletedFlagFalse(String trackingNumber);

    boolean existsByTrackingNumberAndBitDeletedFlagFalse(String trackingNumber);
}