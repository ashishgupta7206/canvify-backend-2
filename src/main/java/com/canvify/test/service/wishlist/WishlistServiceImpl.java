package com.canvify.test.service.wishlist;

import com.canvify.test.dto.wishlist.WishlistItemDTO;
import com.canvify.test.entity.*;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.model.Pagination;
import com.canvify.test.repository.*;
import com.canvify.test.request.product.ProductAndProductVariantResponse;
import com.canvify.test.request.product.ProductVariantRow;
import com.canvify.test.request.wishlist.AddWishlistRequest;
import com.canvify.test.security.UserContext;
import com.canvify.test.service.product.ProductService;
import com.canvify.test.service.product.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserContext userContext;
    private final ProductService productService;

    @Override
    @Transactional
    public ApiResponse<?> addToWishlist(AddWishlistRequest request) {

        Long userId = userContext.getUserId();
        Long productId = request.getProductId();
        Long variantId = request.getProductVariantId();

        // 1) Validate product exists
        Product product = productRepository.findByIdAndBitDeletedFlagFalse(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 2) If variant provided, validate it belongs to product
        ProductVariant variant = null;
        if (variantId != null) {
            variant = variantRepository.findByIdAndBitDeletedFlagFalse(variantId)
                    .orElseThrow(() -> new RuntimeException("Product variant not found"));

            if (!variant.getProduct().getId().equals(productId)) {
                throw new RuntimeException("Variant does not belong to the specified product");
            }
        }

        // 3) Check duplicate (existing non-deleted)
        Optional<Wishlist> existing;
        if (variantId != null) {
            existing = wishlistRepository.findByUserIdAndProductIdAndProductVariantIdAndBitDeletedFlagFalse(userId, productId, variantId);
        } else {
            throw new RuntimeException("Product Variant Not found");
        }

        if (existing.isPresent()) {
            return ApiResponse.success("Already in wishlist"); // idempotent
        }

        // 4) Create and save
        Wishlist wi = new Wishlist();
        User user = new User(); user.setId(userId); // avoid fetch; JPA supports reference by id if managed, but safer to set id only
        wi.setUser(user);
        wi.setProduct(product);
        wi.setProductVariant(variant);
        wishlistRepository.save(wi);

        return ApiResponse.success(null,"Added to wishlist");
    }

    @Override
    @Transactional
    public ApiResponse<?> removeFromWishlist(Long id) {
        Wishlist item = wishlistRepository.findByIdAndBitDeletedFlagFalse(id)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found"));

        if (!item.getUser().getId().equals(userContext.getUserId())) {
            throw new RuntimeException("Not authorized to remove this wishlist item");
        }

        item.setBitDeletedFlag(true);
        wishlistRepository.save(item);

        return ApiResponse.success("Removed from wishlist");
    }

    @Override
    @Transactional
    public ApiResponse<?> removeFromWishlistByProduct(Long productId, Long variantId) {

        Long userId = userContext.getUserId();
        Optional<Wishlist> existing;
        if (variantId != null) {
            existing = wishlistRepository.findByUserIdAndProductIdAndProductVariantIdAndBitDeletedFlagFalse(userId, productId, variantId);
        } else {
            existing = wishlistRepository.findByUserIdAndProductIdAndProductVariantIdAndBitDeletedFlagFalse(userId, productId, null);
        }

        if (existing.isEmpty()) {
            return ApiResponse.error("Wishlist item not found");
        }

        Wishlist item = existing.get();
        item.setBitDeletedFlag(true);
        wishlistRepository.save(item);

        return ApiResponse.success("Removed from wishlist");
    }

    @Override
    public ApiResponse<?> listWishlist(BaseIndexRequest request) {

        var pageable = request.buildPageable();

        Page<Wishlist> page =
                wishlistRepository.findByUserIdAndBitDeletedFlagFalse(
                        userContext.getUserId(),
                        pageable
                );

        List<ProductVariantRow> productVariantRows =
                page.getContent()
                        .stream()
                        .map(w -> new ProductVariantRow(
                                w.getProduct().getId(),
                                w.getProductVariant() != null
                                        ? w.getProductVariant().getId()
                                        : null
                        ))
                        .toList();

        List<ProductAndProductVariantResponse> response =
                productService.getProductAndProductVariant(productVariantRows);

        // continue with your existing logic (batch product fetch, etc.)
        return ApiResponse.success(response, "Wishlist fetched", new Pagination(page));
    }

    @Override
    public List<WishlistItemDTO> listAllForUser() {
        var list = wishlistRepository.findByUserIdAndBitDeletedFlagFalse(userContext.getUserId());
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    private WishlistItemDTO toDto(Wishlist item) {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setId(item.getId());

        Product p = item.getProduct();
        dto.setProductId(p.getId());
        dto.setProductName(p.getName());
        dto.setProductSlug(p.getSlug());
        dto.setProductMainImage(p.getMainImage());

        ProductVariant v = item.getProductVariant();
        if (v != null) {
            dto.setVariantId(v.getId());
            dto.setSku(v.getSku());
            String label = (v.getSize() != null ? v.getSize() : (v.getWeight() != null ? v.getWeight() : ""));
            dto.setVariantLabel(label);
            dto.setStockQty(v.getStockQty());
            // price: choose selling price if available otherwise mrp
            BigDecimal price = v.getPrice() != null ? v.getPrice() : v.getMrp();
            dto.setPrice(price != null ? price.toPlainString() : null);
        } else {
            // if no variant, attempt to get default info; leave variant fields null
            dto.setVariantId(null);
            dto.setSku(null);
            dto.setVariantLabel(null);
            dto.setStockQty(null);
            dto.setPrice(null);
        }

        return dto;
    }
}