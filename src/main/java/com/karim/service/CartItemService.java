package com.karim.service;

import java.util.List;

import com.karim.dto.AddToCartRequest;
import com.karim.dto.CartResponse;
import com.karim.dto.UpdateCartRequest;

public interface CartItemService {

	CartResponse addToCart(Long userId, AddToCartRequest request);

	CartResponse updateCart(UpdateCartRequest request, Long userId);

	void removeItem(Long cartItemId, Long userId);

	List<CartResponse> getUserCart(Long userId);

	void clearCart(Long userId);
}
