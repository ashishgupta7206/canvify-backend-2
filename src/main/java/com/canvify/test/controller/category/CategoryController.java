package com.canvify.test.controller.category;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.request.category.CategoryCreateRequest;
import com.canvify.test.request.category.CategoryUpdateRequest;
import com.canvify.test.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody CategoryCreateRequest req) {
        return ResponseEntity.ok(categoryService.createCategory(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest req) {

        return ResponseEntity.ok(categoryService.updateCategory(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> get(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<?>> getAll(@RequestBody BaseIndexRequest req) {
        return ResponseEntity.ok(categoryService.getAllCategories(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }
}
