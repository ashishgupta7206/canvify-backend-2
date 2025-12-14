package com.canvify.test.service.cart;

import com.canvify.test.dto.cart.CartDTO;
import com.canvify.test.dto.cart.CartItemDTO;
import com.canvify.test.entity.Cart;
import com.canvify.test.entity.CartItem;
import com.canvify.test.entity.ProductVariant;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.repository.CartItemRepository;
import com.canvify.test.repository.CartRepository;
import com.canvify.test.repository.ProductVariantRepository;
import com.canvify.test.request.cart.AddToCartRequest;
import com.canvify.test.request.cart.UpdateCartRequest;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserContext userContext;

    /* =========================================================
       CART RESOLUTION (SINGLE SOURCE OF TRUTH)
       ========================================================= */

    private Cart getOrCreateCart(String guestId) {

        CustomUserDetails user = userContext.getCurrentUser();

        // Logged-in user cart
        if (user != null) {
            return cartRepository.findByUserIdAndBitDeletedFlagFalse(user.getId())
                    .orElseGet(() -> {
                        Cart c = new Cart();
                        com.canvify.test.entity.User u = new com.canvify.test.entity.User();
                        u.setId(user.getId());
                        c.setUser(u);
                        c.setTotalItems(0);
                        c.setTotalAmount(BigDecimal.ZERO);
                        return cartRepository.save(c);
                    });
        }

        // Guest cart
        return cartRepository.findByCartTokenAndBitDeletedFlagFalse(guestId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setCartToken(guestId);
                    c.setTotalItems(0);
                    c.setTotalAmount(BigDecimal.ZERO);
                    return cartRepository.save(c);
                });
    }

    private Cart resolveCart(String guestId) {

        CustomUserDetails user = userContext.getCurrentUser();

        if (user != null) {
            return cartRepository.findByUserIdAndBitDeletedFlagFalse(user.getId()).orElse(null);
        }

        if (guestId == null) return null;

        return cartRepository.findByCartTokenAndBitDeletedFlagFalse(guestId).orElse(null);
    }

    private void validateOwnership(Cart cart, String guestId) {

        CustomUserDetails user = userContext.getCurrentUser();

        if (user != null) {
            if (cart.getUser() == null || !cart.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized cart access");
            }
        } else {
            if (!Objects.equals(cart.getCartToken(), guestId)) {
                throw new RuntimeException("Unauthorized guest cart access");
            }
        }
    }

    /* =========================================================
       ADD TO CART
       ========================================================= */

    @Override
    @Transactional
    public ApiResponse<?> addToCart(AddToCartRequest req, String guestId) {

        Cart cart = getOrCreateCart(guestId);

        ProductVariant variant = variantRepository.findById(req.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        if (variant.getStockQty() != null && req.getQuantity() > variant.getStockQty()) {
            return ApiResponse.error("Requested quantity exceeds available stock");
        }

        CartItem item = cartItemRepository
                .findByCartIdAndProductVariantIdAndBitDeletedFlagFalse(
                        cart.getId(), variant.getId())
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setCart(cart);
                    ci.setProductVariant(variant);
                    ci.setQuantity(0);
                    ci.setPriceAtTime(
                            variant.getPrice() != null ? variant.getPrice() : variant.getMrp()
                    );
                    return ci;
                });

        item.setQuantity(item.getQuantity() + req.getQuantity());
        cartItemRepository.save(item);

        recalcCart(cart);

        return ApiResponse.success(toCartDto(cart), "Added to cart");
    }

    /* =========================================================
       UPDATE CART ITEM
       ========================================================= */

    @Override
    @Transactional
    public ApiResponse<?> updateCartItem(UpdateCartRequest req, String guestId) {

        CartItem item = cartItemRepository.findById(req.getCartItemId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        validateOwnership(item.getCart(), guestId);

        ProductVariant variant = item.getProductVariant();
        if (variant.getStockQty() != null && req.getQuantity() > variant.getStockQty()) {
            return ApiResponse.error("Requested quantity exceeds stock");
        }

        item.setQuantity(req.getQuantity());
        cartItemRepository.save(item);

        recalcCart(item.getCart());

        return ApiResponse.success(toCartDto(item.getCart()), "Cart updated");
    }

    /* =========================================================
       REMOVE ITEM
       ========================================================= */

    @Override
    @Transactional
    public ApiResponse<?> removeItem(Long cartItemId, String guestId) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        validateOwnership(item.getCart(), guestId);

        item.setBitDeletedFlag(true);
        cartItemRepository.save(item);

        recalcCart(item.getCart());

        return ApiResponse.success("Item removed");
    }

    /* =========================================================
       GET CART
       ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getCart(String guestId) {

        Cart cart = resolveCart(guestId);

        if (cart == null) {
            CartDTO empty = new CartDTO();
            empty.setCartToken(guestId);
            empty.setItems(Collections.emptyList());
            empty.setTotalItems(0);
            empty.setTotalAmount(BigDecimal.ZERO);
            return ApiResponse.success(empty, "Cart fetched");
        }

        return ApiResponse.success(toCartDto(cart), "Cart fetched");
    }

    /* =========================================================
       CLEAR CART
       ========================================================= */

    @Override
    @Transactional
    public ApiResponse<?> clearCart(String guestId) {

        Cart cart = resolveCart(guestId);
        if (cart == null) return ApiResponse.success("Cart already empty");

        validateOwnership(cart, guestId);

        List<CartItem> items = cartItemRepository.findByCartIdAndBitDeletedFlagFalse(cart.getId());
        items.forEach(i -> i.setBitDeletedFlag(true));
        cartItemRepository.saveAll(items);

        cart.setTotalItems(0);
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);

        return ApiResponse.success("Cart cleared");
    }

    /* =========================================================
       MERGE GUEST CART INTO USER CART
       ========================================================= */

    @Override
    @Transactional
    public ApiResponse<?> mergeGuestCartIntoUserCart(String guestId) {

        CustomUserDetails user = userContext.getCurrentUser();
        if (user == null) return ApiResponse.error("Login required");

        Cart guestCart = cartRepository.findByCartTokenAndBitDeletedFlagFalse(guestId).orElse(null);
        if (guestCart == null) return ApiResponse.success("No guest cart found");

        Cart userCart = getOrCreateCart(null);

        List<CartItem> guestItems =
                cartItemRepository.findByCartIdAndBitDeletedFlagFalse(guestCart.getId());

        for (CartItem gi : guestItems) {

            CartItem ui = cartItemRepository
                    .findByCartIdAndProductVariantIdAndBitDeletedFlagFalse(
                            userCart.getId(), gi.getProductVariant().getId())
                    .orElse(null);

            if (ui != null) {
                ui.setQuantity(ui.getQuantity() + gi.getQuantity());
                cartItemRepository.save(ui);
                gi.setBitDeletedFlag(true);
            } else {
                gi.setCart(userCart);
                cartItemRepository.save(gi);
            }
        }

        guestCart.setBitDeletedFlag(true);
        cartRepository.save(guestCart);

        recalcCart(userCart);

        return ApiResponse.success(toCartDto(userCart), "Cart merged");
    }

    /* =========================================================
       RECALCULATE TOTALS
       ========================================================= */

    private void recalcCart(Cart cart) {

        List<CartItem> items =
                cartItemRepository.findByCartIdAndBitDeletedFlagFalse(cart.getId());

        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal totalAmount = items.stream()
                .map(i -> i.getPriceAtTime()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalItems(totalItems);
        cart.setTotalAmount(totalAmount);
        cartRepository.save(cart);
    }

    /* =========================================================
       DTO MAPPING
       ========================================================= */

    private CartDTO toCartDto(Cart cart) {

        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getId());
        dto.setCartToken(cart.getCartToken());
        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        dto.setTotalItems(cart.getTotalItems());
        dto.setTotalAmount(cart.getTotalAmount());

        List<CartItemDTO> items = cartItemRepository
                .findByCartIdAndBitDeletedFlagFalse(cart.getId())
                .stream()
                .map(i -> {

                    ProductVariant v = i.getProductVariant();

                    CartItemDTO it = new CartItemDTO();
                    it.setId(i.getId());

                    // ---------- Product ----------
                    it.setProductId(v.getProduct().getId());
                    it.setProductName(v.getProduct().getName());
                    it.setProductSlug(v.getProduct().getSlug());   // ✅ NEW
                    it.setProductImage(v.getProduct().getMainImage());

                    // ---------- Variant ----------
                    it.setVariantId(v.getId());
                    it.setVariantName(v.getName());                // ✅ NEW
                    it.setSku(v.getSku());

                    String label =
                            v.getSize() != null ? v.getSize()
                                    : (v.getWeight() != null ? v.getWeight() : null);
                    it.setVariantLabel(label);

                    // ---------- Pricing ----------
                    it.setQuantity(i.getQuantity());
                    it.setPriceAtTime(i.getPriceAtTime());
                    it.setLineTotal(
                            i.getPriceAtTime().multiply(
                                    BigDecimal.valueOf(i.getQuantity())
                            )
                    );

                    return it;
                })
                .toList();

        dto.setItems(items);
        return dto;
    }

}
