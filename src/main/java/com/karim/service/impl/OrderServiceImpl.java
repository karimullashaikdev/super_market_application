package com.karim.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karim.dto.CheckoutRequest;
import com.karim.dto.OrderItemDetailsDTO;
import com.karim.entity.Address;
import com.karim.entity.CartItem;
import com.karim.entity.Order;
import com.karim.entity.OrderItem;
import com.karim.entity.Product;
import com.karim.enums.OrderStatus;
import com.karim.enums.PaymentStatus;
import com.karim.exception.CartEmptyException;
import com.karim.exception.UnauthorizedException;
import com.karim.repository.AddressRepository;
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

	@Autowired
	private AddressRepository addressRepo;

	// ===============================
	// PAYMENT SUCCESS (IMPORTANT)
	// ===============================
	@Override
	public Order markOrderAsPaid(Long orderId) {

		Order order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

		// ✅ UPDATE STATUS AFTER PAYMENT
		order.setStatus(OrderStatus.PAID);
		order.setPaymentStatus(PaymentStatus.SUCCESS);

		return orderRepo.save(order);
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

	// ===============================
	// ADMIN - ALL ORDERS
	// ===============================
	@Override
	public List<OrderItemDetailsDTO> getAllOrders() {
		return orderRepo.fetchOrderItemDetails();
	}

	@Override
	@Transactional
	public Order checkout(Long userId, CheckoutRequest request) {

		List<CartItem> cartItems = cartRepo.findByUserIdAndDeletedFalse(userId);

		if (cartItems.isEmpty()) {
			throw new CartEmptyException("Cart is empty");
		}

		// ✅ Fetch address entity and validate ownership
		Address deliveryAddress = addressRepo.findByIdAndUserId(request.getAddressId(), userId)
				.orElseThrow(() -> new RuntimeException("Address is required. Please select a delivery address."));

		Order order = new Order();
		order.setUserId(userId);

		// ✅ STATUS FLOW
		order.setStatus(OrderStatus.CREATED);

		// ✅ PAYMENT
		order.setPaymentType(request.getPaymentType());
		order.setPaymentStatus(PaymentStatus.PENDING);

		// ✅ ADDRESS — formatted string for display
		order.setAddress(deliveryAddress.toFormattedString());

		// ✅ Structured address fields — used by tracking page for accurate geocoding
		order.setAddressLine2(deliveryAddress.getLine2()); // locality e.g. "Raj Bhavan Road"
		order.setAddressCity(deliveryAddress.getCity()); // e.g. "Hyderabad"
		order.setAddressState(deliveryAddress.getState()); // e.g. "Telangana"
		order.setAddressPin(deliveryAddress.getPin()); // e.g. "500038"

		// ✅ GPS coordinates — exact pin if customer dropped one on the map
		order.setLatitude(deliveryAddress.getLatitude());
		order.setLongitude(deliveryAddress.getLongitude());

		// ===============================
		// CREATE ORDER ITEMS
		// ===============================
		List<OrderItem> orderItems = cartItems.stream().map(cart -> {

			Product product = productRepo.findById(cart.getProductId())
					.orElseThrow(() -> new RuntimeException("Product not found"));

			// ✅ STOCK VALIDATION
			if (product.getStock() < cart.getQuantity()) {
				throw new RuntimeException("Insufficient stock for product: " + product.getName());
			}

			// ✅ REDUCE STOCK
			product.setStock(product.getStock() - cart.getQuantity());
			productRepo.save(product);

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

		// ✅ CLEAR CART
		cartRepo.deleteByUserId(userId);

		return savedOrder;
	}

	@Override
	public Order findById(Long orderId) {
		return orderRepo.findById(orderId).orElse(null);
	}
}
