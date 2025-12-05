package com.canvify.test.repository;

import com.canvify.test.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndBitDeletedFlagFalse(Long id);
    List<Category> findByParentIdAndBitDeletedFlagFalse(Long parentId);
    Optional<Category> findBySlug(String slug);
    Page<Category> findByBitDeletedFlagFalseAndParentIdIsNull(Pageable pageable);
}
