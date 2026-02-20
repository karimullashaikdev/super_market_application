package com.karim.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karim.dto.AddToCartRequest;
import com.karim.dto.CartResponse;
import com.karim.dto.UpdateCartRequest;
import com.karim.entity.CartItem;
import com.karim.entity.Product;
import com.karim.exception.CartEmptyException;
import com.karim.exception.CartItemNotFoundException;
import com.karim.exception.CartItemRemovedException;
import com.karim.exception.OutOfStockException;
import com.karim.exception.ProductNotFoundException;
import com.karim.exception.UnauthorizedException;
import com.karim.repository.CartItemRepository;
import com.karim.repository.ProductRepository;
import com.karim.service.CartItemService;

@Service
public class CartItemServiceImpl implements CartItemService {

	@Autowired
	private ProductRepository proRepo;
	@Autowired
	private CartItemRepository cartRepo;

	@Override
	public CartResponse addToCart(Long userId, AddToCartRequest request) {

		// 1. Validate Product
		Product product = proRepo.findByIdAndDeletedFalse(request.getProductId()).orElseThrow(
				() -> new ProductNotFoundException("Product not found with id : " + request.getProductId()));

		// 2. Check Stock
		if (product.getStock() < request.getQuantity()) {
			throw new OutOfStockException("Insufficient Stock !!!");
		}

		// 🔥 3. Check if item exists (even deleted)
		CartItem cartItem = cartRepo.findByUserIdAndProductId(userId, request.getProductId()).orElse(null);

		if (cartItem != null) {

			// If previously deleted → reactivate
			if (cartItem.isDeleted()) {

				cartItem.setDeleted(false);
				cartItem.setQuantity(request.getQuantity());

			} else {

				int newQty = cartItem.getQuantity() + request.getQuantity();

				if (product.getStock() < newQty) {
					throw new OutOfStockException("Stock Exceeded !!!");
				}

				cartItem.setQuantity(newQty);
			}

			cartRepo.save(cartItem);

			return mapToResponse(cartItem, product);
		}

		// 4. If not exists → create new
		CartItem item = new CartItem();
		item.setUserId(userId);
		item.setProductId(request.getProductId());
		item.setQuantity(request.getQuantity());
		item.setDeleted(false);

		cartRepo.save(item);

		return mapToResponse(item, product);
	}

	@Override
	public CartResponse updateCart(UpdateCartRequest request, Long userId) {

		// 1. Get active cart item
		CartItem item = cartRepo.findByIdAndDeletedFalse(request.getCartItemId())
				.orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));

		// 2. Authorization check
		if (!item.getUserId().equals(userId)) {
			throw new UnauthorizedException("Access denied, this cart is not belonging to you....");
		}

		// 3. If quantity <= 0 → remove
		if (request.getQuantity() <= 0) {

			item.setDeleted(true);
			cartRepo.save(item);

			throw new CartItemRemovedException("Item removed from cart");
		}

		// 4. Get product
		Product product = proRepo.findByIdAndDeletedFalse(item.getProductId())
				.orElseThrow(() -> new ProductNotFoundException("Product not found"));

		// 5. Check stock
		if (product.getStock() < request.getQuantity()) {
			throw new OutOfStockException("Insufficient stock");
		}

		// 6. Update quantity
		item.setQuantity(request.getQuantity());

		cartRepo.save(item);

		// 7. Return response
		return mapToResponse(item, product);
	}

	@Override
	public void removeItem(Long cartItemId, Long userId) {

		// 1. Get active cart item
		CartItem item = cartRepo.findByIdAndDeletedFalse(cartItemId)
				.orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));

		// 2. Ownership check
		if (!item.getUserId().equals(userId)) {
			throw new UnauthorizedException("Access denied");
		}

		// 3. Soft delete
		item.setDeleted(true);

		cartRepo.save(item);
	}

	@Override
	public List<CartResponse> getUserCart(Long userId) {

		// 1. Get cart items
		List<CartItem> items = cartRepo.findByUserIdAndDeletedFalse(userId);

		if (items.isEmpty()) {
			return List.of();
		}

		// 2. Get all productIds
		List<Long> productIds = items.stream().map(CartItem::getProductId).toList();

		// 3. Fetch products in ONE query
		List<Product> products = proRepo.findByIdInAndDeletedFalse(productIds);

		// 4. Map productId -> Product
		Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

		// 5. Build response
		return items.stream().map(item -> {

			Product product = productMap.get(item.getProductId());

			return mapToResponse(item, product);

		}).toList();
	}

	@Override
	@Transactional
	public void clearCart(Long userId) {

		List<CartItem> items = cartRepo.findByUserIdAndDeletedFalse(userId);

		if (items.isEmpty()) {
			throw new CartEmptyException("Cart is already empty");
		}

		cartRepo.deleteByUserId(userId);
	}

	private CartResponse mapToResponse(CartItem item, Product product) {

		CartResponse res = new CartResponse();

		res.setCartItemId(item.getId());
		res.setProductId(item.getProductId());
		res.setQuantity(item.getQuantity());

		if (product != null) {

			res.setProductName(product.getName());
			res.setPrice(product.getPrice());

			res.setTotalPrice(product.getPrice() * item.getQuantity());
		}

		return res;
	}

}
