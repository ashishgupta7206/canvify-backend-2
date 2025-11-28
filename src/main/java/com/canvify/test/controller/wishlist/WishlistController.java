package com.canvify.test.controller.wishlist;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.request.wishlist.AddWishlistRequest;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.service.wishlist.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addToWishlist(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody AddWishlistRequest request) {

        return ResponseEntity.ok(wishlistService.addToWishlist(currentUser, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> removeFromWishlist(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable("id") Long wishlistItemId) {

        return ResponseEntity.ok(wishlistService.removeFromWishlist(currentUser, wishlistItemId));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> removeFromWishlistByProduct(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam Long productId,
            @RequestParam(required = false) Long productVariantId) {

        return ResponseEntity.ok(wishlistService.removeFromWishlistByProduct(currentUser, productId, productVariantId));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<?>> listWishlist(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody BaseIndexRequest request) {

        return ResponseEntity.ok(wishlistService.listWishlist(currentUser, request));
    }

    // convenience endpoint: get all without pagination
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(@AuthenticationPrincipal CustomUserDetails currentUser) {
        var items = wishlistService.listAllForUser(currentUser);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
}