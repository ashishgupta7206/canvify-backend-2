package com.canvify.test.repository;

import com.canvify.test.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {

    List<DeliveryPartner> findByBitDeletedFlagFalse();

    boolean existsByNameAndBitDeletedFlagFalse(String name);
}