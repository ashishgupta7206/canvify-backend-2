package com.canvify.test.repository;

import com.canvify.test.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartIdAndBitDeletedFlagFalse(Long cartId);

    Optional<CartItem> findByCartIdAndProductVariantIdAndBitDeletedFlagFalse(Long cartId, Long variantId);
}
