package com.canvify.test.service.category;

import com.canvify.test.dto.category.CategoryDTO;
import com.canvify.test.entity.Category;
import com.canvify.test.repository.CategoryRepository;
import com.canvify.test.request.category.CategoryCreateRequest;
import com.canvify.test.request.category.CategoryUpdateRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.Pagination;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.response.category.GetCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    @Override
    public ApiResponse<?> createCategory(CategoryCreateRequest req) {

        Category c = new Category();
        c.setName(req.getName());
        c.setSlug(req.getName().toLowerCase().replace(" ", "-"));
        c.setDescription(req.getDescription());
        c.setImageUrl(req.getImageUrl());

        if (req.getParentId() != null)
            c.setParent(repo.findByIdAndBitDeletedFlagFalse(req.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found")));

        Category category = repo.save(c);

        return ApiResponse.success(category, "Category created successfully");
    }

    @Override
    public ApiResponse<?> updateCategory(Long id, CategoryUpdateRequest req) {

        Category c = repo.findByIdAndBitDeletedFlagFalse(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (req.getName() != null) c.setName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getImageUrl() != null) c.setImageUrl(req.getImageUrl());
        if (req.getParentId() != null)
            c.setParent(repo.findByIdAndBitDeletedFlagFalse(req.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent not found")));

        repo.save(c);

        return ApiResponse.success("Category updated successfully");
    }

    @Override
    public ApiResponse<?> getCategory(Long id) {

        Category c = repo.findByIdAndBitDeletedFlagFalse(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Category> subCategory = repo.findByParentIdAndBitDeletedFlagFalse(id);

        GetCategoryResponse resp = GetCategoryResponse.builder()
                .name(c.getName())
                .id(c.getId())
                .slug(c.getSlug())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .subCategory(subCategory)
                .build();

        return ApiResponse.success(resp);
    }

    @Override
    public ApiResponse<?> getAllCategories(BaseIndexRequest request) {

        Page<Category> page = repo.findAll(request.buildPageable());

        var items = page.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ApiResponse.success(items, "Categories fetched", new Pagination(page));
    }

    @Override
    public ApiResponse<?> getAllParentCategories(BaseIndexRequest request) {

        Page<Category> page = repo.findByBitDeletedFlagFalseAndParentIdIsNull(request.buildPageable());

        var items = page.getContent()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ApiResponse.success(items, " Parent categories fetched", new Pagination(page));
    }

    @Override
    public ApiResponse<?> deleteCategory(Long id) {

        Category c = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        c.setBitDeletedFlag(true);
        repo.save(c);

        return ApiResponse.success("Category deleted");
    }

    private CategoryDTO toDTO(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setSlug(c.getSlug());
        dto.setDescription(c.getDescription());
        dto.setImageUrl(c.getImageUrl());
        dto.setParentId(c.getParent() != null ? c.getParent().getId() : null);
        return dto;
    }
}
