package com.canvify.test.service.product;

import com.canvify.test.dto.product.ProductDTO;
import com.canvify.test.model.base.BaseIndexRequest;
import com.canvify.test.request.product.CreateProductRequest;
import com.canvify.test.request.product.UpdateProductRequest;
import com.canvify.test.response.ApiResponse;

public interface ProductService {

    ApiResponse<?> createProduct(CreateProductRequest request);

    ApiResponse<?> updateProduct(Long id, UpdateProductRequest request);

    ApiResponse<?> getProductById(Long id);

    ApiResponse<?> getAllProducts(BaseIndexRequest request);

    ApiResponse<?> deleteProduct(Long id);
}
