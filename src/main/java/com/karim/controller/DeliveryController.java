package com.karim.controller;

import com.karim.dto.LocationUpdateRequest;
import com.karim.entity.Order;
import com.karim.enums.OrderStatus;
import com.karim.repository.OrderRepository;
import com.karim.repository.UserRepository;
import com.karim.service.DeliveryService;
import com.karim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*")
public class DeliveryController {

    @Autowired private OrderRepository       orderRepo;
    @Autowired private UserRepository        userRepository;
    @Autowired private UserService           userService;
    @Autowired private DeliveryService       deliveryService;
    @Autowired private SimpMessagingTemplate messagingTemplate;  // ✅ for WebSocket broadcast

    // 📦 1. AVAILABLE ORDERS
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableOrders() {
        List<Order> orders = orderRepo.findByDeletedFalse()
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .toList();
        return ResponseEntity.ok(orders);
    }

    // 📦 2. MY ASSIGNED ORDERS
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders() {
        Long deliveryId = userService.getCurrentUserId();
        List<Order> orders = orderRepo.findByDeletedFalse()
                .stream()
                .filter(o -> deliveryId.equals(o.getDeliveryAgentId()))
                .toList();
        return ResponseEntity.ok(orders);
    }

    // ✅ 3. ACCEPT ORDER
    @PostMapping("/accept/{orderId}")
    public ResponseEntity<?> acceptOrder(@PathVariable Long orderId) {
        try {
            deliveryService.acceptOrder(orderId);
            return ResponseEntity.ok("Order accepted");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🚀 4. START DELIVERY
    @PostMapping("/start/{orderId}")
    public ResponseEntity<?> startDelivery(@PathVariable Long orderId) {
        try {
            deliveryService.startDelivery(orderId);
            return ResponseEntity.ok("Delivery started");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 📍 5. COMPLETE DELIVERY
    @PostMapping("/complete/{orderId}")
    public ResponseEntity<?> completeDelivery(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        try {
            String otp = body.get("otp");
            if (otp == null || otp.isBlank())
                return ResponseEntity.badRequest().body("OTP is required");
            deliveryService.completeDelivery(orderId, otp);
            return ResponseEntity.ok("Order delivered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🗺 6. TRACK ORDER — returns both delivery address coords + agent live coords
    @GetMapping("/track/{orderId}")
    public ResponseEntity<?> trackOrder(@PathVariable Long orderId) {
        try {
            Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("orderId",       order.getId());
            response.put("status",        order.getStatus().name());
            response.put("address",       order.getAddress());
            response.put("totalAmount",   order.getTotalAmount());
            response.put("paymentType",   order.getPaymentType() != null
                    ? order.getPaymentType().name() : null);
            response.put("createdAt",     order.getCreatedAt());

            // Delivery address coords + structured parts for geocoding
            response.put("latitude",      order.getLatitude());
            response.put("longitude",     order.getLongitude());
            response.put("addressLine2",  order.getAddressLine2());
            response.put("addressCity",   order.getAddressCity());
            response.put("addressState",  order.getAddressState());
            response.put("addressPin",    order.getAddressPin());

            // ✅ Agent live location — shown as moving bike marker on map
            response.put("agentLatitude",  order.getAgentLatitude());
            response.put("agentLongitude", order.getAgentLongitude());

            if (order.getDeliveryAgentId() != null) {
                userRepository.findById(order.getDeliveryAgentId()).ifPresent(agent -> {
                    response.put("agentId",     "AGT-" + agent.getId());
                    response.put("agentName",   agent.getName());
                    response.put("agentMobile", agent.getMobileNumber());
                });
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🛵 8. AGENT STOPS LOCATION SHARING
    //    POST /api/delivery/location/stop
    //    Broadcasts LOCATION_STOP to customer tracking page → removes bike marker
    @PostMapping("/location/stop")
    public ResponseEntity<?> stopLocation(@RequestBody LocationUpdateRequest req) {
        try {
            Long agentId = userService.getCurrentUserId();
            Order order  = orderRepo.findByIdAndDeletedFalse(req.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            if (!agentId.equals(order.getDeliveryAgentId()))
                return ResponseEntity.status(403).body("Not your order");

            // Clear stored agent location
            order.setAgentLatitude(null);
            order.setAgentLongitude(null);
            orderRepo.save(order);

            // Tell tracking page to remove the bike marker
            Map<String, Object> wsPayload = new LinkedHashMap<>();
            wsPayload.put("type",    "LOCATION_STOP");
            wsPayload.put("orderId", order.getId());
            messagingTemplate.convertAndSend("/topic/order/" + order.getId(), wsPayload);

            return ResponseEntity.ok("Location sharing stopped");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🛵 7. AGENT PUSHES LIVE GPS LOCATION (called every 5s from agent's web page)
    //    POST /api/delivery/location
    //    Body: { "orderId": 14, "latitude": 17.4271, "longitude": 78.4548 }
    @PostMapping("/location")
    public ResponseEntity<?> updateLocation(@RequestBody LocationUpdateRequest req) {
        try {
            Long agentId = userService.getCurrentUserId();

            Order order = orderRepo.findByIdAndDeletedFalse(req.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Only the assigned agent can push location
            if (!agentId.equals(order.getDeliveryAgentId()))
                return ResponseEntity.status(403).body("Not your order");

            // Only when actively delivering
            if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY)
                return ResponseEntity.badRequest().body("Order is not out for delivery");

            // Save latest position
            order.setAgentLatitude(req.getLatitude());
            order.setAgentLongitude(req.getLongitude());
            orderRepo.save(order);

            // Broadcast to customer tracking page via WebSocket
            Map<String, Object> wsPayload = new LinkedHashMap<>();
            wsPayload.put("type",      "LOCATION");
            wsPayload.put("orderId",   order.getId());
            wsPayload.put("latitude",  req.getLatitude());
            wsPayload.put("longitude", req.getLongitude());
            wsPayload.put("status",    order.getStatus().name());

            messagingTemplate.convertAndSend("/topic/order/" + order.getId(), wsPayload);

            return ResponseEntity.ok("Location updated");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}