package com.canvify.test.repository;

import com.canvify.test.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Page<Wishlist> findByUserIdAndBitDeletedFlagFalse(Long userId, Pageable pageable);

    List<Wishlist> findByUserIdAndBitDeletedFlagFalse(Long userId);

    Optional<Wishlist> findByUserIdAndProductIdAndProductVariantIdAndBitDeletedFlagFalse(Long userId, Long productId, Long productVariantId);

    boolean existsByUserIdAndProductIdAndProductVariantIdAndBitDeletedFlagFalse(Long userId, Long productId, Long productVariantId);
}