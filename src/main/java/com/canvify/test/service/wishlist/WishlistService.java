package com.canvify.test.service.wishlist;


import com.canvify.test.dto.wishlist.WishlistItemDTO;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.request.wishlist.AddWishlistRequest;
import com.canvify.test.security.CustomUserDetails;

import java.util.List;

public interface WishlistService {

    ApiResponse<?> addToWishlist(CustomUserDetails currentUser, AddWishlistRequest request);

    ApiResponse<?> removeFromWishlist(CustomUserDetails currentUser, Long wishlistItemId);

    ApiResponse<?> removeFromWishlistByProduct(CustomUserDetails currentUser, Long productId, Long variantId);

    ApiResponse<?> listWishlist(CustomUserDetails currentUser, BaseIndexRequest request);

    // helper for controllers that need raw DTO list
    List<WishlistItemDTO> listAllForUser(CustomUserDetails currentUser);
}