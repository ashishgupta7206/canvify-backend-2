package com.canvify.test.controller;

import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.request.product.CreateProductRequest;
import com.canvify.test.request.product.UpdateProductRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
//@PreAuthorize("ROLE_ADMIN")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createProduct(@Valid @RequestBody CreateProductRequest req) {
        return ResponseEntity.ok(productService.createProduct(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest req) {

        return ResponseEntity.ok(productService.updateProduct(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<?>> getAllProducts(
            @RequestBody BaseIndexRequest request) {

        return ResponseEntity.ok(productService.getAllProducts(request));
    }

    @PostMapping("/product-by-category/{categoryId}")
    public ResponseEntity<ApiResponse<?>> getProductsByCategoryId(
            @PathVariable Long categoryId,
            @RequestBody BaseIndexRequest request) {

        return ResponseEntity.ok(productService.getProductsByCategoryId(categoryId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }
}
