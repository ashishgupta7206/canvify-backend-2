package com.canvify.test.repository;

import com.canvify.test.entity.DeliveryAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {

    List<DeliveryAssignment> findByOrderIdAndBitDeletedFlagFalse(Long orderId);

    Page<DeliveryAssignment> findByPartnerIdAndBitDeletedFlagFalse(Long partnerId, Pageable Pageable);

    Optional<DeliveryAssignment> findByOrderIdAndPartnerIdAndBitDeletedFlagFalse(Long orderId, Long partnerId);
}