package com.karim.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karim.dto.CheckoutRequest;
import com.karim.dto.OrderItemDetailsDTO;
import com.karim.entity.CartItem;
import com.karim.entity.Order;
import com.karim.entity.OrderItem;
import com.karim.entity.Product;
import com.karim.enums.OrderStatus;
import com.karim.exception.CartEmptyException;
import com.karim.exception.UnauthorizedException;
import com.karim.repository.CartItemRepository;
import com.karim.repository.OrderRepository;
import com.karim.repository.ProductRepository;
import com.karim.service.OrderService;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private CartItemRepository cartRepo;

	@Autowired
	private OrderRepository orderRepo;

	@Autowired
	private ProductRepository productRepo;

	// ===============================
	// CHECKOUT
	// ===============================
	@Override
	@Transactional
	public Order checkout(Long userId, CheckoutRequest request) {

		List<CartItem> cartItems = cartRepo.findByUserIdAndDeletedFalse(userId);

		if (cartItems.isEmpty()) {
			throw new CartEmptyException("Cart is empty");
		}

		Order order = new Order();
		order.setUserId(userId);

		// 🔥 Set Enums Properly
		order.setStatus(OrderStatus.PENDING);
		order.setPaymentType(request.getPaymentType());

		List<OrderItem> orderItems = cartItems.stream().map(cart -> {

			// Fetch Product
			Product product = productRepo.findById(cart.getProductId())
					.orElseThrow(() -> new RuntimeException("Product not found"));

			// ✅ Stock Validation (VERY IMPORTANT)
			if (product.getStock() < cart.getQuantity()) {
				throw new RuntimeException("Insufficient stock for product: " + product.getName());
			}

			// ✅ Reduce Stock
			product.setStock(product.getStock() - cart.getQuantity());

			OrderItem item = new OrderItem();
			item.setProductId(product.getId());
			item.setProductName(product.getName());
			item.setPrice(product.getPrice());
			item.setQuantity(cart.getQuantity());
			item.setOrder(order);

			return item;

		}).toList();

		double total = orderItems.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();

		order.setItems(orderItems);
		order.setTotalAmount(total);

		Order savedOrder = orderRepo.save(order);

		// Clear cart after order
		cartRepo.deleteByUserId(userId);

		return savedOrder;
	}

	// ===============================
	// MY ORDERS
	// ===============================
	@Override
	public List<Order> getMyOrders(Long userId) {

		return orderRepo.findByUserIdAndDeletedFalse(userId);
	}

	// ===============================
	// VIEW SINGLE ORDER
	// ===============================
	@Override
	public Order getOrder(Long orderId, Long userId) {

		Order order = orderRepo.findByIdAndDeletedFalse(orderId)
				.orElseThrow(() -> new RuntimeException("Order not found"));

		if (!order.getUserId().equals(userId)) {
			throw new UnauthorizedException("Access denied");
		}

		return order;
	}

	@Override
	public List<OrderItemDetailsDTO> getAllOrders() {
		return orderRepo.fetchOrderItemDetails();
	}
}
