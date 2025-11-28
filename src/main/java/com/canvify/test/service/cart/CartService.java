package com.canvify.test.service.cart;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.cart.AddToCartRequest;
import com.canvify.test.request.cart.UpdateCartRequest;
import com.canvify.test.security.CustomUserDetails;

public interface CartService {

    ApiResponse<?> addToCart(AddToCartRequest req, CustomUserDetails currentUser);

    ApiResponse<?> updateCartItem(UpdateCartRequest req, CustomUserDetails currentUser);

    ApiResponse<?> removeItem(Long cartItemId, CustomUserDetails currentUser);

    ApiResponse<?> getCart(String cartToken, CustomUserDetails currentUser);

    ApiResponse<?> clearCart(String cartToken, CustomUserDetails currentUser);

    /**
     * Merge a guest cart (cartToken) into the logged-in user's cart.
     * Called after successful login on frontend.
     */
    ApiResponse<?> mergeGuestCartIntoUserCart(String cartToken, CustomUserDetails currentUser);
}
