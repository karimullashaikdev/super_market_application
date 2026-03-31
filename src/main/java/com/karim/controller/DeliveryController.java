package com.karim.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
public class DeliveryController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    // 📦 Get all PAID orders
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableOrders() {

        List<Order> orders = orderRepository
                .findByStatusAndDeletedFalse(OrderStatus.PAID);

        return ResponseEntity.ok(orders);
    }

    // ✅ Accept Order
    @PostMapping("/accept/{orderId}")
    public ResponseEntity<?> acceptOrder(@PathVariable Long orderId) {

        Long deliveryId = userService.getCurrentUserId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PAID) {
            return ResponseEntity.badRequest().body("Order not available");
        }

        order.setDeliveryAgentId(deliveryId);
        order.setStatus(OrderStatus.ASSIGNED);

        orderRepository.save(order);

        return ResponseEntity.ok("Order accepted");
    }
}
