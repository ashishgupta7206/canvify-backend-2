package com.canvify.test.repository;

import com.canvify.test.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdAndBitDeletedFlagFalseOrderBySortOrderAsc(Long productId);
    List<ProductImage> findByProductIdAndProductVariantIdIsNull(Long productId);
    List<ProductImage> findByProductVariantId(Long variantId);
    List<ProductImage> findByProductVariantIdAndBitDeletedFlagFalseOrderBySortOrderAsc(Long variantId);
}
