package com.canvify.test.service.category;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.request.category.CategoryCreateRequest;
import com.canvify.test.request.category.CategoryUpdateRequest;

public interface CategoryService {

    ApiResponse<?> createCategory(CategoryCreateRequest req);

    ApiResponse<?> updateCategory(Long id, CategoryUpdateRequest req);

    ApiResponse<?> getCategory(Long id);

    ApiResponse<?> getAllCategories(BaseIndexRequest request);

    ApiResponse<?> deleteCategory(Long id);
}
