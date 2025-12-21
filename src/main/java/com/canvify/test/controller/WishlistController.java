package com.canvify.test.controller;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.request.wishlist.AddWishlistRequest;
import com.canvify.test.service.wishlist.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> addToWishlist(
            @Valid @RequestBody AddWishlistRequest request) {

        return ResponseEntity.ok(wishlistService.addToWishlist(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> removeFromWishlist(
            @PathVariable("id") Long wishlistItemId) {

        return ResponseEntity.ok(wishlistService.removeFromWishlist(wishlistItemId));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> removeFromWishlistByProduct(
            @RequestParam Long productId,
            @RequestParam(required = false) Long productVariantId) {

        return ResponseEntity.ok(wishlistService.removeFromWishlistByProduct(productId, productVariantId));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<?>> listWishlist(
            @RequestBody BaseIndexRequest request) {

        return ResponseEntity.ok(wishlistService.listWishlist(request));
    }

    // convenience endpoint: get all without pagination
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll() {
        var items = wishlistService.listAllForUser();
        return ResponseEntity.ok(ApiResponse.success(items));
    }
}