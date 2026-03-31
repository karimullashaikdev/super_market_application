package com.karim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karim.entity.Order;
import com.karim.enums.OrderStatus;
import com.karim.repository.OrderRepository;
import com.karim.service.UserService;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*")
public class DeliveryController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private UserService userService;

    // =================================
    // 📦 1. AVAILABLE ORDERS (PAID)
    // =================================
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableOrders() {

        List<Order> orders = orderRepo.findByDeletedFalse()
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .toList();

        return ResponseEntity.ok(orders);
    }

    // =================================
    // ✅ 2. ACCEPT ORDER
    // =================================
    @PostMapping("/accept/{orderId}")
    public ResponseEntity<?> acceptOrder(@PathVariable Long orderId) {

        Long deliveryId = userService.getCurrentUserId();

        Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PAID) {
            return ResponseEntity.badRequest().body("Order not available");
        }

        order.setDeliveryAgentId(deliveryId);
        order.setStatus(OrderStatus.ASSIGNED);

        orderRepo.save(order);

        return ResponseEntity.ok("Order accepted");
    }

    // =================================
    // 📦 3. MY ASSIGNED ORDERS
    // =================================
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders() {

        Long deliveryId = userService.getCurrentUserId();

        List<Order> orders = orderRepo.findByDeletedFalse()
                .stream()
                .filter(o -> deliveryId.equals(o.getDeliveryAgentId()))
                .toList();

        return ResponseEntity.ok(orders);
    }

    // =================================
    // 🚀 4. START DELIVERY
    // =================================
    @PostMapping("/start/{orderId}")
    public ResponseEntity<?> startDelivery(@PathVariable Long orderId) {

        Long deliveryId = userService.getCurrentUserId();

        Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!deliveryId.equals(order.getDeliveryAgentId())) {
            return ResponseEntity.status(403).body("Not your order");
        }

        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepo.save(order);

        return ResponseEntity.ok("Delivery started");
    }

    // =================================
    // 📍 5. MARK AS DELIVERED
    // =================================
    @PostMapping("/complete/{orderId}")
    public ResponseEntity<?> completeDelivery(@PathVariable Long orderId) {

        Long deliveryId = userService.getCurrentUserId();

        Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!deliveryId.equals(order.getDeliveryAgentId())) {
            return ResponseEntity.status(403).body("Not your order");
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepo.save(order);

        return ResponseEntity.ok("Order delivered successfully");
    }
}