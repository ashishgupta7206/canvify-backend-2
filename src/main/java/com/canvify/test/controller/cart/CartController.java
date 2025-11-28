package com.canvify.test.controller.cart;

import com.canvify.test.request.cart.AddToCartRequest;
import com.canvify.test.request.cart.UpdateCartRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.service.cart.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> addToCart(
            @Valid @RequestBody AddToCartRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(cartService.addToCart(req, currentUser));
    }

    @PutMapping("/item")
    public ResponseEntity<ApiResponse<?>> updateCartItem(
            @Valid @RequestBody UpdateCartRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(cartService.updateCartItem(req, currentUser));
    }

    @DeleteMapping("/item/{id}")
    public ResponseEntity<ApiResponse<?>> removeItem(
            @PathVariable("id") Long cartItemId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(cartService.removeItem(cartItemId, currentUser));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getCart(
            @RequestParam(required = false) String cartToken,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(cartService.getCart(cartToken, currentUser));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<?>> clearCart(
            @RequestParam(required = false) String cartToken,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(cartService.clearCart(cartToken, currentUser));
    }

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<?>> mergeGuestCart(
            @RequestParam String cartToken,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(cartService.mergeGuestCartIntoUserCart(cartToken, currentUser));
    }
}
