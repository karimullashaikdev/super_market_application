package com.karim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karim.dto.CheckoutRequest;
import com.karim.dto.OrderItemDetailsDTO;
import com.karim.entity.Order;
import com.karim.exception.CartEmptyException;
import com.karim.exception.UnauthorizedException;
import com.karim.service.OrderService;
import com.karim.service.UserService;

@RestController
@RequestMapping("/api/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;

	// ===============================
	// CHECKOUT
	// ===============================
//	@PostMapping("/checkout")
//	public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
//		try {
//			Long userId = userService.getCurrentUserId();
//			Order order = orderService.checkout(userId, request);
//			return ResponseEntity.ok(order);
//
//		} catch (CartEmptyException ex) {
//			return ResponseEntity.badRequest().body(ex.getMessage());
//		} catch (RuntimeException ex) {
//			return ResponseEntity.status(500).body(ex.getMessage());
//		}
//	}
	@PostMapping("/checkout")
	public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
		try {
			Long userId = userService.getCurrentUserId();
			Order order = orderService.checkout(userId, request);
			return ResponseEntity.ok(order);
		} catch (CartEmptyException ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		} catch (RuntimeException ex) {
			ex.printStackTrace(); // ← ADD THIS LINE temporarily
			return ResponseEntity.status(500).body(ex.getMessage());
		}
	}

	// ===============================
	// MY ORDERS
	// ===============================
	@GetMapping("/my")
	public ResponseEntity<?> myOrders() {
		Long userId = userService.getCurrentUserId();
		List<Order> orders = orderService.getMyOrders(userId);

		if (orders.isEmpty()) {
			return ResponseEntity.ok("You have no orders yet");
		}

		return ResponseEntity.ok(orders);
	}

	// ===============================
	// ORDER DETAILS
	// ===============================
	@GetMapping("/{id}")
	public ResponseEntity<?> getOrder(@PathVariable Long id) {
		try {
			Long userId = userService.getCurrentUserId();
			Order order = orderService.getOrder(id, userId);
			return ResponseEntity.ok(order);

		} catch (UnauthorizedException ex) {
			return ResponseEntity.status(403).body(ex.getMessage());
		} catch (RuntimeException ex) {
			return ResponseEntity.status(404).body(ex.getMessage());
		}
	}

//	@GetMapping("/all")
//	public ResponseEntity<List<OrderItemDetailsDTO>> getAllOrders() {
//		List<OrderItemDetailsDTO> allOrders = orderService.getAllOrders();
//		return ResponseEntity.status(HttpStatus.OK).body(allOrders);
//	}
	@GetMapping("/all")
	public ResponseEntity<List<OrderItemDetailsDTO>> getAllOrders() {
		try {
			List<OrderItemDetailsDTO> allOrders = orderService.getAllOrders();
			return ResponseEntity.status(HttpStatus.OK).body(allOrders);
		} catch (Exception e) {
			e.printStackTrace(); // ← ADD THIS
			return ResponseEntity.status(500).body(null);
		}
	}
}
