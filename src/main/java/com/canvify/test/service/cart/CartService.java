package com.canvify.test.service.cart;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.cart.AddToCartRequest;
import com.canvify.test.request.cart.UpdateCartRequest;

public interface CartService {

    ApiResponse<?> addToCart(AddToCartRequest req, String guestId);

    ApiResponse<?> updateCartItem(UpdateCartRequest req, String guestId);

    ApiResponse<?> removeItem(Long cartItemId, String guestId);

    ApiResponse<?> getCart(String guestId);

    ApiResponse<?> clearCart(String guestId);

    /**
     * Merge guest cart into logged-in user's cart.
     * Called immediately after successful login.
     */
    ApiResponse<?> mergeGuestCartIntoUserCart(String guestId);
}
