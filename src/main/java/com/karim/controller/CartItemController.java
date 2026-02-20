package com.karim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karim.dto.AddToCartRequest;
import com.karim.dto.CartResponse;
import com.karim.dto.UpdateCartRequest;
import com.karim.service.CartItemService;
import com.karim.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartItemController {

	@Autowired
	private CartItemService cartService;

	@Autowired
	private UserService userService;

	// ====================================================
	// ADD TO CART
	// ====================================================
	@PostMapping("/add")
	public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {

		Long userId = userService.getCurrentUserId();

		CartResponse response = cartService.addToCart(userId, request);

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	// ====================================================
	// UPDATE CART
	// ====================================================
	@PutMapping("/update")
	public ResponseEntity<CartResponse> updateCart(@Valid @RequestBody UpdateCartRequest request) {

		Long userId = userService.getCurrentUserId();

		CartResponse response = cartService.updateCart(request, userId);

		return ResponseEntity.ok(response);
	}

	// ====================================================
	// REMOVE ITEM
	// ====================================================
	@DeleteMapping("/remove/{cartItemId}")
	public ResponseEntity<String> removeItem(@PathVariable Long cartItemId) {

		Long userId = userService.getCurrentUserId();

		cartService.removeItem(cartItemId, userId);

		return ResponseEntity.ok("Item removed successfully");
	}

	// ====================================================
	// VIEW CART
	// ====================================================
	@GetMapping
	public ResponseEntity<List<CartResponse>> getUserCart() {

		Long userId = userService.getCurrentUserId();

		List<CartResponse> cart = cartService.getUserCart(userId);

		return ResponseEntity.ok(cart);
	}

	// ====================================================
	// CLEAR CART
	// ====================================================
	@DeleteMapping("/clear")
	public ResponseEntity<String> clearCart() {

		Long userId = userService.getCurrentUserId();

		cartService.clearCart(userId);

		return ResponseEntity.ok("Cart cleared successfully");
	}
}
