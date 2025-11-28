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
import com.canvify.test.service.inventory.InventoryService;
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
    private final InventoryService inventoryService; // optional - if present, used for stock checks

    private Cart ensureCartFor(AddToCartRequest req, CustomUserDetails currentUser) {
        if (currentUser != null) {
            // logged-in user's cart
            return cartRepository.findByUserIdAndBitDeletedFlagFalse(currentUser.getId())
                    .orElseGet(() -> {
                        Cart c = new Cart();
                        c.setUser(null); // set below with reference
                        // set user by id reference to avoid extra read
                        com.canvify.test.entity.User u = new com.canvify.test.entity.User();
                        u.setId(currentUser.getId());
                        c.setUser(u);
                        c.setTotalItems(0);
                        c.setTotalAmount(BigDecimal.ZERO.doubleValue());
                        return cartRepository.save(c);
                    });
        } else {
            // guest flow using cartToken
            String cartToken = req.getCartToken();
            if (cartToken == null || cartToken.isBlank()) {
                // create a new token and cart
                cartToken = UUID.randomUUID().toString();
            }
            String finalToken = cartToken;
            return cartRepository.findByCartTokenAndBitDeletedFlagFalse(finalToken)
                    .orElseGet(() -> {
                        Cart c = new Cart();
                        c.setCartToken(finalToken);
                        c.setTotalItems(0);
                        c.setTotalAmount(BigDecimal.ZERO.doubleValue());
                        return cartRepository.save(c);
                    });
        }
    }

    private Cart resolveCart(String cartToken, CustomUserDetails currentUser) {
        if (currentUser != null) {
            return cartRepository.findByUserIdAndBitDeletedFlagFalse(currentUser.getId())
                    .orElse(null);
        }
        if (cartToken == null) return null;
        return cartRepository.findByCartTokenAndBitDeletedFlagFalse(cartToken).orElse(null);
    }

    @Override
    @Transactional
    public ApiResponse<?> addToCart(AddToCartRequest req, CustomUserDetails currentUser) {

        Cart cart;
        if (currentUser != null) {
            cart = cartRepository.findByUserIdAndBitDeletedFlagFalse(currentUser.getId())
                    .orElseGet(() -> {
                        Cart c = new Cart();
                        com.canvify.test.entity.User u = new com.canvify.test.entity.User();
                        u.setId(currentUser.getId());
                        c.setUser(u);
                        c.setTotalItems(0);
                        c.setTotalAmount(0.0);
                        return cartRepository.save(c);
                    });
        } else {
            String token = req.getCartToken();
            if (token == null || token.isBlank()) token = UUID.randomUUID().toString();
            final String finalToken = token;
            cart = cartRepository.findByCartTokenAndBitDeletedFlagFalse(token)
                    .orElseGet(() -> {
                        Cart c = new Cart();
                        c.setCartToken(finalToken);
                        c.setTotalItems(0);
                        c.setTotalAmount(BigDecimal.ZERO.doubleValue());
                        return cartRepository.save(c);
                    });
        }

        ProductVariant variant = variantRepository.findById(req.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        // optional stock check (non-blocking) - if available check available qty
        Integer available = variant.getStockQty();
        if (available != null && req.getQuantity() > available) {
            return ApiResponse.error("Requested quantity exceeds available stock");
        }

        // find existing cart item with same variant
        Optional<CartItem> existingOpt = cartItemRepository.findByCartIdAndProductVariantIdAndBitDeletedFlagFalse(cart.getId(), variant.getId());

        CartItem item;
        if (existingOpt.isPresent()) {
            item = existingOpt.get();
            item.setQuantity(item.getQuantity() + req.getQuantity());
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setProductVariant(variant);
            item.setQuantity(req.getQuantity());
            // snapshot price: prefer price then mrp
            BigDecimal price = variant.getPrice() != null ? variant.getPrice() : variant.getMrp();
            item.setPriceAtTime(price);
        }
        cartItemRepository.save(item);

        // recalc cart totals
        recalcCart(cart);

        // return cart token for guests so clients can keep it
        CartDTO dto = toCartDto(cart);
        if (cart.getCartToken() == null && currentUser == null) {
            // should not happen; ensure token exists
            dto.setCartToken(UUID.randomUUID().toString());
        }
        return ApiResponse.success(dto, "Added to cart");
    }

    @Override
    @Transactional
    public ApiResponse<?> updateCartItem(UpdateCartRequest req, CustomUserDetails currentUser) {
        CartItem item = cartItemRepository.findById(req.getCartItemId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // check ownership: if user logged in, must match; if guest, cartToken must be handled at controller level
        if (currentUser != null && (item.getCart().getUser() == null || !item.getCart().getUser().getId().equals(currentUser.getId()))) {
            throw new RuntimeException("Unauthorized");
        }

        // update quantity with stock check
        ProductVariant variant = item.getProductVariant();
        if (variant.getStockQty() != null && req.getQuantity() > variant.getStockQty()) {
            return ApiResponse.error("Requested quantity exceeds available stock");
        }

        item.setQuantity(req.getQuantity());
        cartItemRepository.save(item);

        recalcCart(item.getCart());

        return ApiResponse.success(toCartDto(item.getCart()), "Cart updated");
    }

    @Override
    @Transactional
    public ApiResponse<?> removeItem(Long cartItemId, CustomUserDetails currentUser) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // ownership
        if (currentUser != null && (item.getCart().getUser() == null || !item.getCart().getUser().getId().equals(currentUser.getId()))) {
            throw new RuntimeException("Unauthorized");
        }

        item.setBitDeletedFlag(true);
        cartItemRepository.save(item);

        recalcCart(item.getCart());

        return ApiResponse.success("Item removed from cart");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getCart(String cartToken, CustomUserDetails currentUser) {
        Cart cart = resolveCart(cartToken, currentUser);
        if (cart == null) {
            // return empty cart DTO (with generated token for guest)
            CartDTO empty = new CartDTO();
            empty.setCartToken(cartToken != null ? cartToken : UUID.randomUUID().toString());
            empty.setItems(Collections.emptyList());
            empty.setTotalItems(0);
            empty.setTotalAmount(BigDecimal.ZERO);
            return ApiResponse.success(empty, "Cart fetched");
        }
        return ApiResponse.success(toCartDto(cart), "Cart fetched");
    }

    @Override
    @Transactional
    public ApiResponse<?> clearCart(String cartToken, CustomUserDetails currentUser) {
        Cart cart = resolveCart(cartToken, currentUser);
        if (cart == null) return ApiResponse.success("Cart already empty");

        // soft delete items
        List<CartItem> items = cartItemRepository.findByCartIdAndBitDeletedFlagFalse(cart.getId());
        for (CartItem item : items) {
            item.setBitDeletedFlag(true);
        }
        cartItemRepository.saveAll(items);

        cart.setTotalAmount(0.0);
        cart.setTotalItems(0);
        cartRepository.save(cart);

        return ApiResponse.success("Cart cleared");
    }

    @Override
    @Transactional
    public ApiResponse<?> mergeGuestCartIntoUserCart(String cartToken, CustomUserDetails currentUser) {
        if (cartToken == null) return ApiResponse.error("cartToken required");

        Cart guestCart = cartRepository.findByCartTokenAndBitDeletedFlagFalse(cartToken).orElse(null);
        if (guestCart == null) return ApiResponse.success("No guest cart found");

        Cart userCart = cartRepository.findByUserIdAndBitDeletedFlagFalse(currentUser.getId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    com.canvify.test.entity.User u = new com.canvify.test.entity.User();
                    u.setId(currentUser.getId());
                    c.setUser(u);
                    c.setTotalAmount(0.0);
                    c.setTotalItems(0);
                    return cartRepository.save(c);
                });

        List<CartItem> guestItems = cartItemRepository.findByCartIdAndBitDeletedFlagFalse(guestCart.getId());
        for (CartItem gi : guestItems) {
            Optional<CartItem> existing = cartItemRepository.findByCartIdAndProductVariantIdAndBitDeletedFlagFalse(userCart.getId(), gi.getProductVariant().getId());
            if (existing.isPresent()) {
                CartItem ui = existing.get();
                ui.setQuantity(ui.getQuantity() + gi.getQuantity());
                cartItemRepository.save(ui);
            } else {
                // move guest item to user cart
                gi.setCart(userCart);
                cartItemRepository.save(gi);
            }
        }

        // mark guest cart deleted (soft)
        guestCart.setBitDeletedFlag(true);
        cartRepository.save(guestCart);

        // recalc user cart totals
        recalcCart(userCart);

        return ApiResponse.success(toCartDto(userCart), "Cart merged");
    }

    // recalc totals and persist
    private void recalcCart(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartIdAndBitDeletedFlagFalse(cart.getId());

        int totalItems = items.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal totalAmount = items.stream()
                .map(i -> (i.getPriceAtTime() != null ? i.getPriceAtTime() : BigDecimal.ZERO).multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalItems(totalItems);
        cart.setTotalAmount(totalAmount.doubleValue());
        cartRepository.save(cart);
    }

    private CartDTO toCartDto(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getId());
        dto.setCartToken(cart.getCartToken());
        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        dto.setTotalItems(cart.getTotalItems() != null ? cart.getTotalItems() : 0);
        dto.setTotalAmount(BigDecimal.valueOf(cart.getTotalAmount() != null ? cart.getTotalAmount() : 0.0));

        List<CartItemDTO> items = cartItemRepository.findByCartIdAndBitDeletedFlagFalse(cart.getId())
                .stream()
                .map(i -> {
                    CartItemDTO it = new CartItemDTO();
                    it.setId(i.getId());
                    ProductVariant v = i.getProductVariant();
                    it.setVariantId(v.getId());
                    it.setSku(v.getSku());
                    it.setProductId(v.getProduct() != null ? v.getProduct().getId() : null);
                    it.setProductName(v.getProduct() != null ? v.getProduct().getName() : null);
                    it.setProductImage(v.getProduct() != null ? v.getProduct().getMainImage() : null);
                    String label = v.getSize() != null ? v.getSize() : (v.getWeight() != null ? v.getWeight() : null);
                    it.setVariantLabel(label);
                    it.setQuantity(i.getQuantity());
                    it.setPriceAtTime(i.getPriceAtTime());
                    BigDecimal line = (i.getPriceAtTime() != null ? i.getPriceAtTime() : BigDecimal.ZERO).multiply(BigDecimal.valueOf(i.getQuantity()));
                    it.setLineTotal(line);
                    return it;
                })
                .collect(Collectors.toList());

        dto.setItems(items);
        return dto;
    }
}
