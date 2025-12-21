package com.canvify.test.controller;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.cart.AddToCartRequest;
import com.canvify.test.request.cart.UpdateCartRequest;
import com.canvify.test.service.cart.CartService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /* =========================================================
       ADD TO CART
       ========================================================= */

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> addToCart(
            @Valid @RequestBody AddToCartRequest req,
            @CookieValue(value = "GUEST_ID", required = false) String guestId,
            HttpServletResponse response
    ) {

        // Create guest ID if not present
        if (guestId == null) {
            guestId = UUID.randomUUID().toString();
            setGuestCookie(response, guestId);
        }

        return ResponseEntity.ok(cartService.addToCart(req, guestId));
    }

    /* =========================================================
       UPDATE CART ITEM
       ========================================================= */

    @PutMapping("/item")
    public ResponseEntity<ApiResponse<?>> updateCartItem(
            @Valid @RequestBody UpdateCartRequest req,
            @CookieValue(value = "GUEST_ID", required = false) String guestId
    ) {
        return ResponseEntity.ok(cartService.updateCartItem(req, guestId));
    }

    /* =========================================================
       REMOVE CART ITEM
       ========================================================= */

    @DeleteMapping("/item/{id}")
    public ResponseEntity<ApiResponse<?>> removeItem(
            @PathVariable("id") Long cartItemId,
            @CookieValue(value = "GUEST_ID", required = false) String guestId
    ) {
        return ResponseEntity.ok(cartService.removeItem(cartItemId, guestId));
    }

    /* =========================================================
       GET CART
       ========================================================= */

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getCart(
            @CookieValue(value = "GUEST_ID", required = false) String guestId,
            HttpServletResponse response
    ) {

        // Ensure guest has an ID so cart persists
        if (guestId == null) {
            guestId = UUID.randomUUID().toString();
            setGuestCookie(response, guestId);
        }

        return ResponseEntity.ok(cartService.getCart(guestId));
    }

    /* =========================================================
       CLEAR CART
       ========================================================= */

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<?>> clearCart(
            @CookieValue(value = "GUEST_ID", required = false) String guestId
    ) {
        return ResponseEntity.ok(cartService.clearCart(guestId));
    }

    /* =========================================================
       MERGE GUEST CART INTO USER CART
       (Call this immediately after login)
       ========================================================= */

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<?>> mergeGuestCart(
            @CookieValue(value = "GUEST_ID", required = false) String guestId,
            HttpServletResponse response
    ) {

        ApiResponse<?> result = cartService.mergeGuestCartIntoUserCart(guestId);

        // Clear guest cookie after successful merge
        deleteGuestCookie(response);

        return ResponseEntity.ok(result);
    }

    /* =========================================================
       COOKIE HELPERS
       ========================================================= */

    private void setGuestCookie(HttpServletResponse response, String guestId) {

        ResponseCookie cookie = ResponseCookie.from("GUEST_ID", guestId)
                .httpOnly(true)
                .secure(true)        // set false only for local http testing
                .path("/")
                .maxAge(60 * 60 * 24 * 30) // 30 days
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteGuestCookie(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from("GUEST_ID", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
