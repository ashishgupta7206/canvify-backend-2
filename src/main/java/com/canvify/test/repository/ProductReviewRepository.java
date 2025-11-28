package com.canvify.test.repository;

import com.canvify.test.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductIdAndBitDeletedFlagFalse(Long productId);

    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product.id = :productId")
    Double findAvgRatingByProductId(Long productId);
}
