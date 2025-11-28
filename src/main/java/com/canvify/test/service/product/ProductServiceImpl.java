package com.canvify.test.service.product;

import com.canvify.test.dto.product.ProductDTO;
import com.canvify.test.entity.*;
import com.canvify.test.enums.ProductStatus;
import com.canvify.test.model.base.BaseIndexRequest;
import com.canvify.test.repository.*;
import com.canvify.test.request.product.*;
import com.canvify.test.response.ApiResponse;
import com.canvify.test.response.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductImageRepository imageRepo;

    @Override
    public ApiResponse<?> createProduct(CreateProductRequest req) {

        Category category = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setName(req.getName());
        product.setSlug(req.getName().toLowerCase().replace(" ", "-"));
        product.setShortDescription(req.getShortDescription());
        product.setLongDescription(req.getLongDescription());
        product.setMainImage(req.getMainImage());
        product.setStatus(req.getStatus());
        product.setCategory(category);

        productRepo.save(product);

        // Save variants
        if (req.getVariants() != null) {
            for (ProductVariantRequest v : req.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setSku(v.getSku());
                variant.setPrice(v.getPrice());
                variant.setMrp(v.getMrp());
                variant.setDiscountPercent(v.getDiscountPercent());
                variant.setStockQty(v.getStockQty());
                variant.setSize(v.getSize());
                variant.setWeight(v.getWeight());
                variant.setColor(v.getColor());
                variant.setBarcode(v.getBarcode());
                variantRepo.save(variant);
            }
        }

        // Save images
        if (req.getImages() != null) {
            for (ProductImageRequest img : req.getImages()) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl(img.getImageUrl());
                image.setSortOrder(img.getSortOrder());
                imageRepo.save(image);
            }
        }

        return ApiResponse.success("Product created successfully");
    }

    @Override
    public ApiResponse<?> updateProduct(Long id, UpdateProductRequest req) {

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (req.getName() != null) product.setName(req.getName());
        if (req.getShortDescription() != null) product.setShortDescription(req.getShortDescription());
        if (req.getLongDescription() != null) product.setLongDescription(req.getLongDescription());
        if (req.getMainImage() != null) product.setMainImage(req.getMainImage());
        if (req.getStatus() != null) product.setStatus(req.getStatus());

        if (req.getCategoryId() != null) {
            Category category = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        productRepo.save(product);

        return ApiResponse.success("Product updated successfully");
    }

    @Override
    public ApiResponse<?> getProductById(Long id) {

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductDTO dto = convertToDTO(product);

        return ApiResponse.success(dto);
    }

    @Override
    public ApiResponse<?> getAllProducts(BaseIndexRequest request) {

        Page<Product> page = productRepo.findAll(request.buildPageable());

        var dtos = page.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ApiResponse.success(
                dtos,
                "Products fetched",
                new Pagination(page)
        );
    }

    @Override
    public ApiResponse<?> deleteProduct(Long id) {

        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setBitDeletedFlag(true);
        productRepo.save(product);

        return ApiResponse.success("Product deleted");
    }

    // MAPPER
    private ProductDTO convertToDTO(Product product) {

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setShortDescription(product.getShortDescription());
        dto.setLongDescription(product.getLongDescription());
        dto.setMainImage(product.getMainImage());
        dto.setStatus(product.getStatus());
        dto.setCategoryId(product.getCategory().getId());

        dto.setVariants(
                variantRepo.findByProductIdAndBitDeletedFlagFalse(product.getId())
                        .stream()
                        .map(v -> {
                            var vd = new ProductVariantDTO();
                            vd.setId(v.getId());
                            vd.setSku(v.getSku());
                            vd.setPrice(v.getPrice());
                            vd.setMrp(v.getMrp());
                            vd.setDiscountPercent(v.getDiscountPercent());
                            vd.setStockQty(v.getStockQty());
                            vd.setSize(v.getSize());
                            vd.setWeight(v.getWeight());
                            vd.setColor(v.getColor());
                            vd.setBarcode(v.getBarcode());
                            return vd;
                        })
                        .collect(Collectors.toList())
        );

        dto.setImages(
                imageRepo.findByProductIdAndBitDeletedFlagFalseOrderBySortOrderAsc(product.getId())
                        .stream()
                        .map(i -> {
                            var idto = new ProductImageDTO();
                            idto.setId(i.getId());
                            idto.setImageUrl(i.getImageUrl());
                            idto.setSortOrder(i.getSortOrder());
                            return idto;
                        })
                        .collect(Collectors.toList())
        );

        return dto;
    }
}
