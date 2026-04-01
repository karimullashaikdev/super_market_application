package com.karim.controller;

import com.karim.entity.Order;
import com.karim.enums.OrderStatus;
import com.karim.entity.User;
import com.karim.repository.OrderRepository;
import com.karim.repository.UserRepository;
import com.karim.service.DeliveryService;
import com.karim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*")
public class DeliveryController {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DeliveryService deliveryService;

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
    // 📦 2. MY ASSIGNED ORDERS
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
    // ✅ 3. ACCEPT ORDER
    //    → Assigns agent, generates OTP, sends email to customer
    // =================================
    @PostMapping("/accept/{orderId}")
    public ResponseEntity<?> acceptOrder(@PathVariable Long orderId) {
        try {
            deliveryService.acceptOrder(orderId);
            return ResponseEntity.ok("Order accepted");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =================================
    // 🚀 4. START DELIVERY
    // =================================
    @PostMapping("/start/{orderId}")
    public ResponseEntity<?> startDelivery(@PathVariable Long orderId) {
        try {
            deliveryService.startDelivery(orderId);
            return ResponseEntity.ok("Delivery started");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =================================
    // 📍 5. MARK AS DELIVERED (OTP required)
    //    Body: { "otp": "1234" }
    // =================================
    @PostMapping("/complete/{orderId}")
    public ResponseEntity<?> completeDelivery(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        try {
            String otp = body.get("otp");
            if (otp == null || otp.isBlank()) {
                return ResponseEntity.badRequest().body("OTP is required");
            }
            deliveryService.completeDelivery(orderId, otp);
            return ResponseEntity.ok("Order delivered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =================================
    // 🗺 6. TRACK ORDER (public — for customer tracking page)
    //    GET /api/delivery/track/{orderId}
    //    Returns status + agent info (no OTP, no sensitive data)
    // =================================
    @GetMapping("/track/{orderId}")
    public ResponseEntity<?> trackOrder(@PathVariable Long orderId) {
        try {
            Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("orderId", order.getId());
            response.put("status", order.getStatus().name());
            response.put("address", order.getAddress());
            response.put("totalAmount", order.getTotalAmount());
            response.put("paymentType", order.getPaymentType());
            response.put("createdAt", order.getCreatedAt());

            // Include agent info only if assigned
            if (order.getDeliveryAgentId() != null) {
                userRepository.findById(order.getDeliveryAgentId()).ifPresent(agent -> {
                    response.put("agentId", "AGT-" + agent.getId());
                    response.put("agentName", agent.getName());
                    response.put("agentMobile", agent.getMobileNumber());
                });
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}