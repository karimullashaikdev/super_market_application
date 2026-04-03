package com.karim.service.impl;

import com.karim.dto.OrderStatusEvent;
import com.karim.entity.DeliveryOtp;
import com.karim.entity.Order;
import com.karim.entity.User;
import com.karim.enums.OrderStatus;
import com.karim.repository.DeliveryOtpRepository;
import com.karim.repository.OrderRepository;
import com.karim.repository.UserRepository;
import com.karim.service.DeliveryService;
import com.karim.service.EmailService;
import com.karim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired private OrderRepository orderRepo;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private DeliveryOtpRepository deliveryOtpRepository;
    @Autowired private EmailService emailService;

    // ✅ WebSocket broadcaster — Spring injects this automatically
    //    once WebSocketConfig.java is in place
    @Autowired private SimpMessagingTemplate messagingTemplate;

    private static final int MAX_OTP_ATTEMPTS = 3;

    // =================================
    // ✅ 1. ACCEPT ORDER
    // =================================
    @Override
    @Transactional
    public void acceptOrder(Long orderId) {

        Long agentId = userService.getCurrentUserId();

        Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PAID)
            throw new RuntimeException("Order not available for pickup");

        if (order.getDeliveryAgentId() != null)
            throw new RuntimeException("Order is already assigned to another agent");

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        User customer = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        order.setDeliveryAgentId(agentId);
        order.setStatus(OrderStatus.ASSIGNED);
        orderRepo.save(order);

        // Generate & save OTP
        String otp = generateOtp();
        DeliveryOtp deliveryOtp = new DeliveryOtp();
        deliveryOtp.setOrderId(orderId);
        deliveryOtp.setOtp(otp);
        deliveryOtpRepository.save(deliveryOtp);

        // Send email (async — existing, unchanged)
        emailService.sendDeliveryAssignedEmail(
                customer.getEmail(),
                customer.getName(),
                order.getId(),
                order.getTotalAmount(),
                order.getPaymentType() != null ? order.getPaymentType().name() : "—",
                order.getAddress(),
                "AGT-" + agent.getId(),
                agent.getName(),
                agent.getMobileNumber() != null ? agent.getMobileNumber() : "—",
                otp
        );

        // 📡 Broadcast to:
        //   1. All agents on the dashboard → order disappears from "Available"
        //   2. Customer tracking page → status updates instantly
        broadcastStatus(order, agent);
    }

    // =================================
    // 🚀 2. START DELIVERY
    // =================================
    @Override
    @Transactional
    public void startDelivery(Long orderId) {

        Long agentId = userService.getCurrentUserId();

        Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!agentId.equals(order.getDeliveryAgentId()))
            throw new RuntimeException("Not your order");

        if (order.getStatus() != OrderStatus.ASSIGNED)
            throw new RuntimeException("Order must be ASSIGNED before starting delivery");

        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepo.save(order);

        // 📡 Broadcast status change
        User agent = userRepository.findById(agentId).orElse(null);
        broadcastStatus(order, agent);
    }

    // =================================
    // 📦 3. COMPLETE DELIVERY (OTP)
    // =================================
    @Override
    @Transactional
    public void completeDelivery(Long orderId, String enteredOtp) {

        Long agentId = userService.getCurrentUserId();

        Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!agentId.equals(order.getDeliveryAgentId()))
            throw new RuntimeException("Not your order");

        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY)
            throw new RuntimeException("Order is not out for delivery");

        DeliveryOtp deliveryOtp = deliveryOtpRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("OTP not found for this order"));

        if (deliveryOtp.isVerified())
            throw new RuntimeException("OTP already used for this order");

        if (deliveryOtp.getAttempts() >= MAX_OTP_ATTEMPTS)
            throw new RuntimeException("OTP locked after " + MAX_OTP_ATTEMPTS + " failed attempts. Contact support.");

        if (!deliveryOtp.getOtp().equals(enteredOtp.trim())) {
            deliveryOtp.setAttempts(deliveryOtp.getAttempts() + 1);
            deliveryOtpRepository.save(deliveryOtp);
            int remaining = MAX_OTP_ATTEMPTS - deliveryOtp.getAttempts();
            if (remaining > 0)
                throw new RuntimeException("Incorrect OTP. " + remaining + " attempt(s) remaining.");
            else
                throw new RuntimeException("Incorrect OTP. OTP is now locked. Please contact support.");
        }

        // ✅ Correct OTP
        deliveryOtp.setVerified(true);
        deliveryOtpRepository.save(deliveryOtp);

        order.setStatus(OrderStatus.DELIVERED);
        orderRepo.save(order);

        // 📡 Broadcast final delivery status
        User agent = userRepository.findById(agentId).orElse(null);
        broadcastStatus(order, agent);
    }

    // =================================
    // 📡 BROADCAST HELPER
    //    Sends to two topics:
    //    /topic/orders          → delivery-dashboard.html (all agents)
    //    /topic/order/{orderId} → tracking.html (specific customer)
    // =================================
    private void broadcastStatus(Order order, User agent) {
        OrderStatusEvent event = new OrderStatusEvent(
                order.getId(),
                order.getStatus().name(),
                agent != null ? "AGT-" + agent.getId() : null,
                agent != null ? agent.getName() : null,
                agent != null ? agent.getMobileNumber() : null
        );

        // Dashboard — all agents see the update immediately
        messagingTemplate.convertAndSend("/topic/orders", event);

        // Customer tracking page — only that order's subscriber gets it
        messagingTemplate.convertAndSend("/topic/order/" + order.getId(), event);
    }

    // =================================
    // HELPER — 4-digit secure OTP
    // =================================
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }
}