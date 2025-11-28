package com.canvify.test.repository;

import com.canvify.test.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdAndBitDeletedFlagFalse(Long productId);
    Optional<ProductVariant> findBySku(String sku);
}
