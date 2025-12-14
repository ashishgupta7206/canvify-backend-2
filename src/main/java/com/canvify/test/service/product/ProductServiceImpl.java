package com.canvify.test.service.product;

import com.canvify.test.dto.product.ProductDTO;
import com.canvify.test.dto.product.ProductImageDTO;
import com.canvify.test.dto.product.ProductVariantDTO;
import com.canvify.test.entity.*;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.model.Pagination;
import com.canvify.test.repository.*;
import com.canvify.test.request.product.*;
import com.canvify.test.response.product.ProductImageResponse;
import com.canvify.test.response.product.ProductResponse;
import com.canvify.test.response.product.ProductVariantResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductImageRepository imageRepo;

    @Override
    @Transactional
    public ApiResponse<?> createProduct(CreateProductRequest req) {

        Category category = categoryRepo.findByIdAndBitDeletedFlagFalse(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // --------------------------
        // 1. Create Product
        // --------------------------
        Product product = new Product();
        product.setName(req.getName());
        product.setSlug(req.getName().toLowerCase().replace(" ", "-"));
        product.setShortDescription(req.getShortDescription());
        product.setLongDescription(req.getLongDescription());
        product.setMainImage(req.getMainImage());
        product.setStatus(req.getStatus());
        product.setCategory(category);

        productRepo.save(product);

        // --------------------------
        // 2. Create Variants + Variant Images
        // --------------------------
        List<ProductVariant> savedVariants = new ArrayList<>();

        if (req.getVariants() != null) {
            for (ProductVariantRequest v : req.getVariants()) {

                ProductVariant variant = new ProductVariant();
                variant.setName(v.getName());
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
                savedVariants.add(variant);

                // Save variant images
                if (v.getImages() != null) {
                    for (ProductImageRequest imgReq : v.getImages()) {

                        ProductImage image = new ProductImage();
                        image.setProduct(product);            // always set product
                        image.setProductVariant(variant);     // variant-level association
                        image.setImageUrl(imgReq.getImageUrl());
                        image.setSortOrder(imgReq.getSortOrder());

                        imageRepo.save(image);
                    }
                }
            }
        }

        // --------------------------
        // 3. Create PRODUCT-LEVEL Images
        // --------------------------
        if (req.getImages() != null) {
            for (ProductImageRequest imgReq : req.getImages()) {

                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setProductVariant(null); // product-level
                image.setImageUrl(imgReq.getImageUrl());
                image.setSortOrder(imgReq.getSortOrder());

                imageRepo.save(image);
            }
        }

        // --------------------------
        // 4. Build Response (ProductResponse)
        // --------------------------
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setShortDescription(product.getShortDescription());
        response.setLongDescription(product.getLongDescription());
        response.setMainImage(product.getMainImage());
        response.setStatus(product.getStatus().name());

        response.setCategoryId(category.getId());
        response.setCategoryName(category.getName());

        // ---- Product-level images
        List<ProductImageResponse> productImages = imageRepo.findByProductIdAndProductVariantIdIsNull(product.getId())
                .stream()
                .map(img -> {
                    ProductImageResponse r = new ProductImageResponse();
                    r.setId(img.getId());
                    r.setImageUrl(img.getImageUrl());
                    r.setSortOrder(img.getSortOrder());
                    return r;
                })
                .toList();

        response.setImages(productImages);

        // ---- Variant responses
        List<ProductVariantResponse> variantResponses =
                savedVariants.stream().map(variant -> {

                    ProductVariantResponse vr = new ProductVariantResponse();
                    vr.setId(variant.getId());
                    vr.setName(variant.getName());
                    vr.setSku(variant.getSku());
                    vr.setPrice(variant.getPrice());
                    vr.setMrp(variant.getMrp());
                    vr.setDiscountPercent(variant.getDiscountPercent());
                    vr.setStockQty(variant.getStockQty());
                    vr.setSize(variant.getSize());
                    vr.setWeight(variant.getWeight());
                    vr.setColor(variant.getColor());
                    vr.setBarcode(variant.getBarcode());

                    // variant-level images
                    List<ProductImageResponse> variantImages =
                            imageRepo.findByProductVariantId(variant.getId())
                                    .stream()
                                    .map(img -> {
                                        ProductImageResponse ir = new ProductImageResponse();
                                        ir.setId(img.getId());
                                        ir.setImageUrl(img.getImageUrl());
                                        ir.setSortOrder(img.getSortOrder());
                                        return ir;
                                    })
                                    .toList();

                    vr.setImages(variantImages);

                    return vr;
                }).toList();

        response.setVariants(variantResponses);

        // --------------------------
        // 5. Return Success with Data
        // --------------------------
        return ApiResponse.success(response);
    }



    @Override
    @Transactional
    public ApiResponse<?> updateProduct(Long id, UpdateProductRequest req) {

        Product product = productRepo.findByIdAndBitDeletedFlagFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // -------------------------
        // 1. Update product fields
        // -------------------------
        if (req.getName() != null) product.setName(req.getName());
        if (req.getShortDescription() != null) product.setShortDescription(req.getShortDescription());
        if (req.getLongDescription() != null) product.setLongDescription(req.getLongDescription());
        if (req.getMainImage() != null) product.setMainImage(req.getMainImage());
        if (req.getStatus() != null) product.setStatus(req.getStatus());

        if (req.getCategoryId() != null) {
            Category category = categoryRepo.findByIdAndBitDeletedFlagFalse(req.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        productRepo.save(product);

        // -------------------------
        // 2. Update variants
        // -------------------------
        if (req.getVariants() != null) {
            for (ProductVariantUpdateRequest v : req.getVariants()) {

                if (v.getId() == null) {
                    // CREATE NEW VARIANT
                    ProductVariant newVariant = new ProductVariant();
                    newVariant.setProduct(product);
                    newVariant.setSku(v.getSku());
                    newVariant.setPrice(v.getPrice());
                    newVariant.setMrp(v.getMrp());
                    newVariant.setDiscountPercent(v.getDiscountPercent());
                    newVariant.setStockQty(v.getStockQty());
                    newVariant.setSize(v.getSize());
                    newVariant.setWeight(v.getWeight());
                    newVariant.setColor(v.getColor());
                    newVariant.setBarcode(v.getBarcode());

                    variantRepo.save(newVariant);

                    // Save images for this new variant
                    if (v.getImages() != null) {
                        for (ProductImageUpdateRequest img : v.getImages()) {
                            ProductImage newImg = new ProductImage();
                            newImg.setProduct(product);
                            newImg.setProductVariant(newVariant);
                            newImg.setImageUrl(img.getImageUrl());
                            newImg.setSortOrder(img.getSortOrder());
                            imageRepo.save(newImg);
                        }
                    }

                    continue;
                }

                // UPDATE OR DELETE EXISTING VARIANT
                ProductVariant variant = variantRepo.findById(v.getId())
                        .orElseThrow(() -> new RuntimeException("Variant not found"));

                if (Boolean.TRUE.equals(v.getDeleteFlag())) {
                    variant.setBitDeletedFlag(true);
                    variantRepo.save(variant);
                    continue;
                }

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

                // -------------------------
                // Update variant images
                // -------------------------
                if (v.getImages() != null) {
                    for (ProductImageUpdateRequest img : v.getImages()) {

                        if (img.getId() == null) {
                            // NEW IMAGE
                            ProductImage newImg = new ProductImage();
                            newImg.setProduct(product);
                            newImg.setProductVariant(variant);
                            newImg.setImageUrl(img.getImageUrl());
                            newImg.setSortOrder(img.getSortOrder());
                            imageRepo.save(newImg);
                            continue;
                        }

                        ProductImage image = imageRepo.findById(img.getId())
                                .orElseThrow(() -> new RuntimeException("Variant image not found"));

                        if (Boolean.TRUE.equals(img.getDeleteFlag())) {
                            image.setBitDeletedFlag(true);
                            imageRepo.save(image);
                            continue;
                        }

                        image.setImageUrl(img.getImageUrl());
                        image.setSortOrder(img.getSortOrder());
                        imageRepo.save(image);
                    }
                }
            }
        }

        // -------------------------
        // 3. Update PRODUCT LEVEL images
        // -------------------------
        if (req.getImages() != null) {
            for (ProductImageRequest img : req.getImages()) {

                if (img.getId() == null) {
                    // NEW IMAGE
                    ProductImage newImg = new ProductImage();
                    newImg.setProduct(product);
                    newImg.setProductVariant(null);
                    newImg.setImageUrl(img.getImageUrl());
                    newImg.setSortOrder(img.getSortOrder());
                    imageRepo.save(newImg);
                    continue;
                }

                ProductImage existing = imageRepo.findById(img.getId())
                        .orElseThrow(() -> new RuntimeException("Product image not found"));

                existing.setImageUrl(img.getImageUrl());
                existing.setSortOrder(img.getSortOrder());
                imageRepo.save(existing);
            }
        }

        // -------------------------
        // 4. Build ProductResponse (same as createProduct)
        // -------------------------
        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setShortDescription(product.getShortDescription());
        response.setLongDescription(product.getLongDescription());
        response.setMainImage(product.getMainImage());
        response.setStatus(product.getStatus().name());

        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());

        // ---- Product Images
        List<ProductImageResponse> productImages =
                imageRepo.findByProductIdAndProductVariantIdIsNull(product.getId())
                        .stream()
                        .map(img -> {
                            ProductImageResponse r = new ProductImageResponse();
                            r.setId(img.getId());
                            r.setImageUrl(img.getImageUrl());
                            r.setSortOrder(img.getSortOrder());
                            return r;
                        })
                        .toList();
        response.setImages(productImages);

        // ---- Variants + variant images
        List<ProductVariantResponse> variantResponses =
                variantRepo.findByProductIdAndBitDeletedFlagFalse(product.getId())
                        .stream()
                        .map(variant -> {

                            ProductVariantResponse vr = new ProductVariantResponse();
                            vr.setId(variant.getId());
                            vr.setSku(variant.getSku());
                            vr.setPrice(variant.getPrice());
                            vr.setMrp(variant.getMrp());
                            vr.setDiscountPercent(variant.getDiscountPercent());
                            vr.setStockQty(variant.getStockQty());
                            vr.setSize(variant.getSize());
                            vr.setWeight(variant.getWeight());
                            vr.setColor(variant.getColor());
                            vr.setBarcode(variant.getBarcode());

                            List<ProductImageResponse> variantImgs =
                                    imageRepo.findByProductVariantId(variant.getId())
                                            .stream()
                                            .map(img -> {
                                                ProductImageResponse ir = new ProductImageResponse();
                                                ir.setId(img.getId());
                                                ir.setImageUrl(img.getImageUrl());
                                                ir.setSortOrder(img.getSortOrder());
                                                return ir;
                                            })
                                            .toList();

                            vr.setImages(variantImgs);

                            return vr;
                        })
                        .toList();

        response.setVariants(variantResponses);

        // -------------------------
        // 5. Return Response
        // -------------------------
        return ApiResponse.success(response);
    }



    @Override
    public ApiResponse<?> getProductById(Long id) {

        Product product = productRepo.findByIdAndBitDeletedFlagFalse(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductResponse response = buildProductResponse(product);

        return ApiResponse.success(response);
    }

    public List<ProductResponse> getProductByIds(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("Product IDs cannot be empty");
        }

        List<Product> products =
                productRepo.findByIdInAndBitDeletedFlagFalse(ids);

        if (products.isEmpty()) {
            throw new RuntimeException("No products found");
        }

        List<ProductResponse> responses =
                products.stream()
                        .map(this::buildProductResponse)
                        .toList();

        return responses;
    }

//



    @Override
    public ApiResponse<?> getAllProducts(BaseIndexRequest request) {

        Page<Product> page = productRepo.findAll(request.buildPageable());

        List<ProductResponse> responses =
                page.getContent()
                        .stream()
                        .map(this::buildProductResponse)
                        .toList();

        return ApiResponse.success(
                responses,
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

    @Override
    public List<ProductAndProductVariantResponse> getProductAndProductVariant(
            List<ProductVariantRow> request) {

        if (request == null || request.isEmpty()) {
            return List.of();
        }

        // 1️⃣ Collect unique productIds & variantIds
        List<Long> productIds = request.stream()
                .map(ProductVariantRow::getProductId)
                .distinct()
                .toList();

        List<Long> variantIds = request.stream()
                .map(ProductVariantRow::getVariantId)
                .filter(v -> v != null)
                .distinct()
                .toList();

        // 2️⃣ Fetch products
        var productMap = productRepo.findByIdInAndBitDeletedFlagFalse(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 3️⃣ Fetch variants
        var variantMap = variantRepo.findByIdInAndBitDeletedFlagFalse(variantIds)
                .stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        // 4️⃣ Build response per row
        List<ProductAndProductVariantResponse> responses = new ArrayList<>();

        for (ProductVariantRow row : request) {

            Product product = productMap.get(row.getProductId());
            if (product == null) continue;

            ProductAndProductVariantResponse res =
                    new ProductAndProductVariantResponse();

            // ---------------- Product ----------------
            res.setId(product.getId());
            res.setName(product.getName());
            res.setSlug(product.getSlug());
            res.setShortDescription(product.getShortDescription());
            res.setLongDescription(product.getLongDescription());
            res.setMainImage(product.getMainImage());
            res.setStatus(product.getStatus().name());

            res.setCategoryId(product.getCategory().getId());
            res.setCategoryName(product.getCategory().getName());

            // ---------------- Product Images ----------------
            List<ProductImageResponse> productImages =
                    imageRepo.findByProductIdAndProductVariantIdIsNull(product.getId())
                            .stream()
                            .map(img -> {
                                ProductImageResponse r = new ProductImageResponse();
                                r.setId(img.getId());
                                r.setImageUrl(img.getImageUrl());
                                r.setSortOrder(img.getSortOrder());
                                return r;
                            })
                            .toList();

            res.setImages(productImages);

            // ---------------- Single Variant ----------------
            if (row.getVariantId() != null) {

                ProductVariant variant = variantMap.get(row.getVariantId());
                if (variant != null) {

                    ProductVariantResponse vr = new ProductVariantResponse();
                    vr.setId(variant.getId());
                    vr.setName(variant.getName());
                    vr.setSku(variant.getSku());
                    vr.setPrice(variant.getPrice());
                    vr.setMrp(variant.getMrp());
                    vr.setDiscountPercent(variant.getDiscountPercent());
                    vr.setStockQty(variant.getStockQty());
                    vr.setSize(variant.getSize());
                    vr.setWeight(variant.getWeight());
                    vr.setColor(variant.getColor());
                    vr.setBarcode(variant.getBarcode());

                    List<ProductImageResponse> variantImages =
                            imageRepo.findByProductVariantId(variant.getId())
                                    .stream()
                                    .map(img -> {
                                        ProductImageResponse ir = new ProductImageResponse();
                                        ir.setId(img.getId());
                                        ir.setImageUrl(img.getImageUrl());
                                        ir.setSortOrder(img.getSortOrder());
                                        return ir;
                                    })
                                    .toList();

                    vr.setImages(variantImages);
                    res.setVariant(vr);
                }
            }

            responses.add(res);
        }

        return responses;
    }


    private ProductResponse buildProductResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setSlug(product.getSlug());
        response.setShortDescription(product.getShortDescription());
        response.setLongDescription(product.getLongDescription());
        response.setMainImage(product.getMainImage());
        response.setStatus(product.getStatus().name());

        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());

        // --------------------------
        // Product-level Images
        // --------------------------
        List<ProductImageResponse> productImages =
                imageRepo.findByProductIdAndProductVariantIdIsNull(product.getId())
                        .stream()
                        .map(img -> {
                            ProductImageResponse r = new ProductImageResponse();
                            r.setId(img.getId());
                            r.setImageUrl(img.getImageUrl());
                            r.setSortOrder(img.getSortOrder());
                            return r;
                        })
                        .toList();

        response.setImages(productImages);

        // --------------------------
        // Variants + Variant Images
        // --------------------------
        List<ProductVariantResponse> variantResponses =
                variantRepo.findByProductIdAndBitDeletedFlagFalse(product.getId())
                        .stream()
                        .map(variant -> {

                            ProductVariantResponse vr = new ProductVariantResponse();
                            vr.setId(variant.getId());
                            vr.setSku(variant.getSku());
                            vr.setPrice(variant.getPrice());
                            vr.setMrp(variant.getMrp());
                            vr.setDiscountPercent(variant.getDiscountPercent());
                            vr.setStockQty(variant.getStockQty());
                            vr.setSize(variant.getSize());
                            vr.setWeight(variant.getWeight());
                            vr.setColor(variant.getColor());
                            vr.setBarcode(variant.getBarcode());

                            List<ProductImageResponse> variantImages =
                                    imageRepo.findByProductVariantId(variant.getId())
                                            .stream()
                                            .map(img -> {
                                                ProductImageResponse ir = new ProductImageResponse();
                                                ir.setId(img.getId());
                                                ir.setImageUrl(img.getImageUrl());
                                                ir.setSortOrder(img.getSortOrder());
                                                return ir;
                                            })
                                            .toList();

                            vr.setImages(variantImages);

                            return vr;
                        })
                        .toList();

        response.setVariants(variantResponses);

        return response;
    }

}


//public List<ProductResponse> getProductVariantsByIds(List<Long> variantIds) {
//
//        if (variantIds == null || variantIds.isEmpty()) {
//            throw new RuntimeException("Variant IDs cannot be empty");
//        }
//
//        // 1️⃣ Fetch variants directly
//        List<ProductVariant> variants =
//                variantRepo.findByIdInAndBitDeletedFlagFalse(variantIds);
//
//        if (variants.isEmpty()) {
//            throw new RuntimeException("No variants found");
//        }
//
//        // 2️⃣ Group variants by Product
//        return variants.stream()
//                .collect(Collectors.groupingBy(ProductVariant::getProduct))
//                .entrySet()
//                .stream()
//                .map(entry -> {
//
//                    Product product = entry.getKey();
//                    List<ProductVariant> productVariants = entry.getValue();
//
//                    ProductResponse response = new ProductResponse();
//
//                    response.setId(product.getId());
//                    response.setName(product.getName());
//                    response.setSlug(product.getSlug());
//                    response.setShortDescription(product.getShortDescription());
//                    response.setLongDescription(product.getLongDescription());
//                    response.setMainImage(product.getMainImage());
//                    response.setStatus(product.getStatus().name());
//
//                    response.setCategoryId(product.getCategory().getId());
//                    response.setCategoryName(product.getCategory().getName());
//
//                    // --------------------------
//                    // Product-level images
//                    // --------------------------
//                    List<ProductImageResponse> productImages =
//                            imageRepo.findByProductIdAndProductVariantIdIsNull(product.getId())
//                                    .stream()
//                                    .map(img -> {
//                                        ProductImageResponse r = new ProductImageResponse();
//                                        r.setId(img.getId());
//                                        r.setImageUrl(img.getImageUrl());
//                                        r.setSortOrder(img.getSortOrder());
//                                        return r;
//                                    })
//                                    .toList();
//
//                    response.setImages(productImages);
//
//                    // --------------------------
//                    // ONLY requested variants
//                    // --------------------------
//                    List<ProductVariantResponse> variantResponses =
//                            productVariants.stream()
//                                    .map(variant -> {
//
//                                        ProductVariantResponse vr = new ProductVariantResponse();
//                                        vr.setId(variant.getId());
//                                        vr.setName(variant.getName());
//                                        vr.setSku(variant.getSku());
//                                        vr.setPrice(variant.getPrice());
//                                        vr.setMrp(variant.getMrp());
//                                        vr.setDiscountPercent(variant.getDiscountPercent());
//                                        vr.setStockQty(variant.getStockQty());
//                                        vr.setSize(variant.getSize());
//                                        vr.setWeight(variant.getWeight());
//                                        vr.setColor(variant.getColor());
//                                        vr.setBarcode(variant.getBarcode());
//
//                                        List<ProductImageResponse> variantImages =
//                                                imageRepo.findByProductVariantId(variant.getId())
//                                                        .stream()
//                                                        .map(img -> {
//                                                            ProductImageResponse ir = new ProductImageResponse();
//                                                            ir.setId(img.getId());
//                                                            ir.setImageUrl(img.getImageUrl());
//                                                            ir.setSortOrder(img.getSortOrder());
//                                                            return ir;
//                                                        })
//                                                        .toList();
//
//                                        vr.setImages(variantImages);
//
//                                        return vr;
//                                    })
//                                    .toList();
//
//                    response.setVariants(variantResponses);
//
//                    return response;
//
//                })
//                .toList();
//    }
