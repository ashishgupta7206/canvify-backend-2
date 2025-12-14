package com.canvify.test.service.wishlist;


import com.canvify.test.dto.wishlist.WishlistItemDTO;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.BaseIndexRequest;
import com.canvify.test.request.wishlist.AddWishlistRequest;

import java.util.List;

public interface WishlistService {

    ApiResponse<?> addToWishlist(AddWishlistRequest request);

    ApiResponse<?> removeFromWishlist(Long id);

    ApiResponse<?> removeFromWishlistByProduct(Long productId, Long variantId);

    ApiResponse<?> listWishlist(BaseIndexRequest request);

    // helper for controllers that need raw DTO list
    List<WishlistItemDTO> listAllForUser();
}
