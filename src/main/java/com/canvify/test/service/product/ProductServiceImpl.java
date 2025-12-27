package com.canvify.test.service.product;

import com.canvify.test.dto.product.ProductDTO;
import com.canvify.test.dto.product.ProductImageDTO;
import com.canvify.test.dto.product.ProductVariantDTO;
import com.canvify.test.entity.*;
import com.canvify.test.enums.productVariantMktStatus;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.model.Pagination;
import com.canvify.test.repository.*;
import com.canvify.test.request.product.*;
import com.canvify.test.request.product.ProductAndProductVariantResponse;
import com.canvify.test.response.product.*;
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
    private final NutritionInfoRepository nutritionInfoRepo;

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
                if (v.getIsActive() != null) variant.setIsActive(v.getIsActive());
                variant.setCategorySortOrder(v.getCategorySortOrder());
                variant.setRating(v.getRating());
                variant.setStorageInstructions(v.getStorageInstructions());
                variant.setProductVariantMktStatus(v.getProductVariantMktStatus());
                variant.setProductVariantMktStatusSortOrder(v.getProductVariantMktStatusSortOrder());
                variant.setSortOrder(v.getSortOrder());
                variant.setProductType(v.getProductType());

                if (v.getListOfVariantInCombo() != null && !v.getListOfVariantInCombo().isEmpty()) {
                    List<ProductVariant> comboVariants = variantRepo.findByIdInAndBitDeletedFlagFalse(v.getListOfVariantInCombo());
                    variant.setListOfVariantInCombo(comboVariants);
                }

                if (v.getNutritionInfoId() != null) {
                    NutritionInfo nutritionInfo = nutritionInfoRepo.findById(v.getNutritionInfoId())
                            .orElseThrow(() -> new RuntimeException("Nutrition Info not found"));
                    variant.setNutritionInfo(nutritionInfo);
                }

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
                    vr.setIsActive(variant.getIsActive());
                    vr.setCategorySortOrder(variant.getCategorySortOrder());
                    if (variant.getNutritionInfo() != null) {
                        vr.setNutritionInfoId(variant.getNutritionInfo().getId());
                    }
                    vr.setRating(variant.getRating());
                    vr.setStorageInstructions(variant.getStorageInstructions());
                    vr.setProductVariantMktStatus(variant.getProductVariantMktStatus());
                    vr.setProductVariantMktStatusSortOrder(variant.getProductVariantMktStatusSortOrder());
                    vr.setSortOrder(variant.getSortOrder());
                    vr.setProductType(variant.getProductType());
                    if (variant.getListOfVariantInCombo() != null) {
                        vr.setListOfVariantInCombo(variant.getListOfVariantInCombo().stream().map(ProductVariant::getId).collect(Collectors.toList()));
                    }

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
                    if (v.getIsActive() != null) newVariant.setIsActive(v.getIsActive());
                    newVariant.setCategorySortOrder(v.getCategorySortOrder());
                    newVariant.setRating(v.getRating());
                    newVariant.setStorageInstructions(v.getStorageInstructions());
                    newVariant.setProductVariantMktStatus(v.getProductVariantMktStatus());
                    newVariant.setProductVariantMktStatusSortOrder(v.getProductVariantMktStatusSortOrder());
                    newVariant.setSortOrder(v.getSortOrder());
                    newVariant.setProductType(v.getProductType());

                    if (v.getListOfVariantInCombo() != null && !v.getListOfVariantInCombo().isEmpty()) {
                        List<ProductVariant> comboVariants = variantRepo.findByIdInAndBitDeletedFlagFalse(v.getListOfVariantInCombo());
                        newVariant.setListOfVariantInCombo(comboVariants);
                    }

                    if (v.getNutritionInfoId() != null) {
                        NutritionInfo nutritionInfo = nutritionInfoRepo.findById(v.getNutritionInfoId())
                                .orElseThrow(() -> new RuntimeException("Nutrition Info not found"));
                        newVariant.setNutritionInfo(nutritionInfo);
                    }

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

                if (v.getSku() != null) variant.setSku(v.getSku());
                if (v.getPrice() != null) variant.setPrice(v.getPrice());
                if (v.getMrp() != null) variant.setMrp(v.getMrp());
                if (v.getDiscountPercent() != null) variant.setDiscountPercent(v.getDiscountPercent());
                if (v.getStockQty() != null) variant.setStockQty(v.getStockQty());
                if (v.getSize() != null) variant.setSize(v.getSize());
                if (v.getWeight() != null) variant.setWeight(v.getWeight());
                if (v.getColor() != null) variant.setColor(v.getColor());
                if (v.getBarcode() != null) variant.setBarcode(v.getBarcode());
                if (v.getIsActive() != null) variant.setIsActive(v.getIsActive());
                if (v.getCategorySortOrder() != null) variant.setCategorySortOrder(v.getCategorySortOrder());
                if (v.getRating() != null) variant.setRating(v.getRating());
                if (v.getStorageInstructions() != null) variant.setStorageInstructions(v.getStorageInstructions());
                if (v.getProductVariantMktStatus() != null) variant.setProductVariantMktStatus(v.getProductVariantMktStatus());
                if (v.getProductVariantMktStatusSortOrder() != null) variant.setProductVariantMktStatusSortOrder(v.getProductVariantMktStatusSortOrder());
                if (v.getSortOrder() != null) variant.setSortOrder(v.getSortOrder());
                if (v.getProductType() != null) variant.setProductType(v.getProductType());

                if (v.getListOfVariantInCombo() != null) {
                    List<ProductVariant> comboVariants = variantRepo.findByIdInAndBitDeletedFlagFalse(v.getListOfVariantInCombo());
                    variant.setListOfVariantInCombo(comboVariants);
                }

                if (v.getNutritionInfoId() != null) {
                    NutritionInfo nutritionInfo = nutritionInfoRepo.findById(v.getNutritionInfoId())
                            .orElseThrow(() -> new RuntimeException("Nutrition Info not found"));
                    variant.setNutritionInfo(nutritionInfo);
                }

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
                            vr.setIsActive(variant.getIsActive());
                            vr.setCategorySortOrder(variant.getCategorySortOrder());
                            if (variant.getNutritionInfo() != null) {
                                vr.setNutritionInfoId(variant.getNutritionInfo().getId());
                            }
                            vr.setRating(variant.getRating());
                            vr.setStorageInstructions(variant.getStorageInstructions());
                            vr.setProductVariantMktStatus(variant.getProductVariantMktStatus());
                            vr.setProductVariantMktStatusSortOrder(variant.getProductVariantMktStatusSortOrder());
                            vr.setSortOrder(variant.getSortOrder());
                            vr.setProductType(variant.getProductType());
                            if (variant.getListOfVariantInCombo() != null) {
                                vr.setListOfVariantInCombo(variant.getListOfVariantInCombo().stream().map(ProductVariant::getId).collect(Collectors.toList()));
                            }

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

        ProductByIdResponse response = buildProductResponseForById(product);

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
    public ApiResponse<?> getAllProducts(GetAllProductsRequest request) {

        Page<Product> page = productRepo.findAll(
                ProductSpecification.filter(request),
                request.buildPageable()
        );

        List<getAllProductResponse> responses =
                page.getContent()
                        .stream()
                        .flatMap(product ->
                                buildProductGetAllResponses(product).stream()
                        )
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
                            vd.setIsActive(v.getIsActive());
                            vd.setCategorySortOrder(v.getCategorySortOrder());
                            if (v.getNutritionInfo() != null) {
                                vd.setNutritionInfoId(v.getNutritionInfo().getId());
                            }
                            vd.setRating(v.getRating());
                            vd.setStorageInstructions(v.getStorageInstructions());
                            vd.setProductVariantMktStatus(v.getProductVariantMktStatus());
                            vd.setProductVariantMktStatusSortOrder(v.getProductVariantMktStatusSortOrder());
                            vd.setSortOrder(v.getSortOrder());
                            vd.setProductType(v.getProductType());
                            if (v.getListOfVariantInCombo() != null) {
                                vd.setListOfVariantInCombo(v.getListOfVariantInCombo().stream().map(ProductVariant::getId).collect(Collectors.toList()));
                            }
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
                    vr.setIsActive(variant.getIsActive());
                    vr.setCategorySortOrder(variant.getCategorySortOrder());
                    if (variant.getNutritionInfo() != null) {
                        vr.setNutritionInfoId(variant.getNutritionInfo().getId());
                    }
                    vr.setRating(variant.getRating());
                    vr.setStorageInstructions(variant.getStorageInstructions());
                    vr.setProductVariantMktStatus(variant.getProductVariantMktStatus());
                    vr.setProductVariantMktStatusSortOrder(variant.getProductVariantMktStatusSortOrder());
                    vr.setSortOrder(variant.getSortOrder());
                    vr.setProductType(variant.getProductType());
                    if (variant.getListOfVariantInCombo() != null) {
                        vr.setListOfVariantInCombo(variant.getListOfVariantInCombo().stream().map(ProductVariant::getId).collect(Collectors.toList()));
                    }

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

    @Override
    public ApiResponse<?> getProductsByCategoryId(Long categoryId, BaseIndexRequest request) {

        Page<Product> page = productRepo.findProductsByCategory(categoryId, request.buildPageable());

        List<ProductResponse> responses =
                page.getContent()
                        .stream()
                        .map(this::buildProductResponse)
                        .toList();

        return ApiResponse.success(
                responses,
                "Products fetched by category",
                new Pagination(page)
        );
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
                            vr.setIsActive(variant.getIsActive());
                            vr.setCategorySortOrder(variant.getCategorySortOrder());
                            if (variant.getNutritionInfo() != null) {
                                vr.setNutritionInfoId(variant.getNutritionInfo().getId());
                            }
                            vr.setRating(variant.getRating());
                            vr.setStorageInstructions(variant.getStorageInstructions());
                            vr.setProductVariantMktStatus(variant.getProductVariantMktStatus());
                            vr.setProductVariantMktStatusSortOrder(variant.getProductVariantMktStatusSortOrder());
                            vr.setSortOrder(variant.getSortOrder());
                            vr.setProductType(variant.getProductType());
                            if (variant.getListOfVariantInCombo() != null) {
                                vr.setListOfVariantInCombo(variant.getListOfVariantInCombo().stream().map(ProductVariant::getId).collect(Collectors.toList()));
                            }

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

    private ProductByIdResponse buildProductResponseForById(Product product) {

        ProductByIdResponse response = new ProductByIdResponse();

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
        List<ProductVariantByIdResponse> variantResponses =
                variantRepo.findByProductIdAndBitDeletedFlagFalse(product.getId())
                        .stream()
                        .map(variant -> {

                            ProductVariantByIdResponse vr = new ProductVariantByIdResponse();
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
                            vr.setIsActive(variant.getIsActive());
                            vr.setCategorySortOrder(variant.getCategorySortOrder());
                            if (variant.getNutritionInfo() != null) {

                                NutritionInfo ni = variant.getNutritionInfo();

                                NutritionInfoResponse nir = new NutritionInfoResponse();
                                nir.setId(ni.getId());
                                nir.setEnergyKcal(ni.getEnergyKcal());
                                nir.setProteinG(ni.getProteinG());
                                nir.setCarbohydrateG(ni.getCarbohydrateG());
                                nir.setTotalSugarG(ni.getTotalSugarsG());
                                nir.setAddedSugarG(ni.getAddedSugarsG());
                                nir.setTotalFatG(ni.getTotalFatG());
                                nir.setSaturatedFatG(ni.getSaturatedFatG());
                                nir.setTransFatG(ni.getTransFatG());
                                nir.setCholesterolMg(ni.getCholesterolMg());
                                nir.setSodiumMg(ni.getSodiumMg());
                                nir.setIngredients(ni.getIngredients());
                                nir.setServingSize(ni.getServingSize());

                                vr.setNutritionInfo(nir);
                            }
                            vr.setRating(variant.getRating());
                            vr.setStorageInstructions(variant.getStorageInstructions());
                            vr.setProductVariantMktStatus(variant.getProductVariantMktStatus());
                            vr.setProductVariantMktStatusSortOrder(variant.getProductVariantMktStatusSortOrder());
                            vr.setSortOrder(variant.getSortOrder());
                            vr.setProductType(variant.getProductType());
                            if (variant.getListOfVariantInCombo() != null) {
                                vr.setListOfVariantInCombo(variant.getListOfVariantInCombo().stream().map(ProductVariant::getId).collect(Collectors.toList()));
                            }

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

    private List<getAllProductResponse> buildProductGetAllResponses(Product product) {

        List<getAllProductResponse> responses = new ArrayList<>();

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

        // --------------------------
        // Variants
        // --------------------------
        List<ProductVariant> variants =
                variantRepo.findByProductIdAndBitDeletedFlagFalse(product.getId());

        for (ProductVariant variant : variants) {

            getAllProductResponse response = new getAllProductResponse();

            // --------------------------
            // Product fields
            // --------------------------
            response.setProductId(product.getId());
            response.setProductName(product.getName());
            response.setProductSlug(product.getSlug());
            response.setShortDescription(product.getShortDescription());
            response.setLongDescription(product.getLongDescription());
            response.setMainImage(product.getMainImage());
            response.setStatus(product.getStatus().name());

            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());

            // --------------------------
            // Variant fields
            // --------------------------
            response.setProductVariantId(variant.getId());
            response.setProductVariantName(variant.getName());
            response.setProductVariantSku(variant.getSku());
            response.setPrice(variant.getPrice());
            response.setMrp(variant.getMrp());
            response.setDiscountPercent(variant.getDiscountPercent());
            response.setStockQty(variant.getStockQty());
            response.setSize(variant.getSize());
            response.setWeight(variant.getWeight());
            response.setColor(variant.getColor());
            response.setBarcode(variant.getBarcode());
            response.setProductVariantIsActive(variant.getIsActive());
            response.setCategorySortOrder(variant.getCategorySortOrder());
            response.setProductVariantRating(variant.getRating());
            response.setProductVariantMktStatus(variant.getProductVariantMktStatus());
            response.setProductVariantMktStatusSortOrder(
                    variant.getProductVariantMktStatusSortOrder()
            );
            response.setSortOrder(variant.getSortOrder());
            response.setProductType(variant.getProductType());

            if (variant.getListOfVariantInCombo() != null) {
                response.setListOfVariantInCombo(
                        variant.getListOfVariantInCombo()
                                .stream()
                                .map(ProductVariant::getId)
                                .toList()
                );
            }

            // --------------------------
            // Images (variant preferred)
            // --------------------------
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

            response.setImages(
                    !variantImages.isEmpty() ? variantImages : productImages
            );

            responses.add(response);
        }

        return responses;
    }


}
