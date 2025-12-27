package com.canvify.test.repository;

import com.canvify.test.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findAllByStatusAndBitDeletedFlagFalse(Short status, Pageable pageable);
    Optional<Product> findBySlugAndBitDeletedFlagFalse(String slug);
    Optional<Product> findByIdAndBitDeletedFlagFalse(Long id);
    List<Product> findByIdInAndBitDeletedFlagFalse(List<Long> ids);
    @Query(
            value = """
    SELECT p
    FROM Product p
    JOIN FETCH p.category
    WHERE p.category.id = :categoryId
    AND p.bitDeletedFlag = false
  """,
            countQuery = """
    SELECT COUNT(p)
    FROM Product p
    WHERE p.category.id = :categoryId
    AND p.bitDeletedFlag = false
  """
    )
    Page<Product> findProductsByCategory(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );


}
