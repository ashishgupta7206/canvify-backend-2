package com.canvify.test.repository;

import com.canvify.test.entity.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {
    List<StockLedger> findByProductVariantIdOrderByCreatedDateDesc(Long variantId);
}
