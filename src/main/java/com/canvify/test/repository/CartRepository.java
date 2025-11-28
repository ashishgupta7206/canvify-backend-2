package com.canvify.test.repository;

import com.canvify.test.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCartTokenAndBitDeletedFlagFalse(String cartToken);

    Optional<Cart> findByUserIdAndBitDeletedFlagFalse(Long userId);
}
