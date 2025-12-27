package com.canvify.test.service.product;

import com.canvify.test.dto.product.ProductDTO;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.request.product.CreateProductRequest;
import com.canvify.test.request.product.ProductAndProductVariantResponse;
import com.canvify.test.request.product.ProductVariantRow;
import com.canvify.test.request.product.UpdateProductRequest;
import com.canvify.test.model.ApiResponse;

import java.util.List;

public interface ProductService {

    ApiResponse<?> createProduct(CreateProductRequest request);

    ApiResponse<?> updateProduct(Long id, UpdateProductRequest request);

    ApiResponse<?> getProductById(Long id);

    ApiResponse<?> getAllProducts(BaseIndexRequest request);

    ApiResponse<?> deleteProduct(Long id);

    List<ProductAndProductVariantResponse> getProductAndProductVariant(List<ProductVariantRow> request);

    ApiResponse<?> getProductsByCategoryId(Long categoryId, BaseIndexRequest request);
}
