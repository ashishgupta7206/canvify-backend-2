package com.canvify.test.repository;

import com.canvify.test.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByStatusAndBitDeletedFlagFalse(Short status, Pageable pageable);
    Optional<Product> findBySlugAndBitDeletedFlagFalse(String slug);
    Optional<Product> findByIdAndBitDeletedFlagFalse(Long id);
}
