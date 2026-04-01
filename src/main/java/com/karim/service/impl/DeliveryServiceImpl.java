package com.karim.service.impl;

import com.karim.entity.Order;
import com.karim.enums.OrderStatus;
import com.karim.entity.DeliveryOtp;
import com.karim.repository.DeliveryOtpRepository;
import com.karim.repository.OrderRepository;
import com.karim.repository.UserRepository;
import com.karim.service.DeliveryService;
import com.karim.service.EmailService;
import com.karim.service.UserService;
import com.karim.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DeliveryOtpRepository deliveryOtpRepository;

    @Autowired
    private EmailService emailService;

    private static final int MAX_OTP_ATTEMPTS = 3;

    // =================================
    // ✅ 1. ACCEPT ORDER
    //    → Assigns agent, generates OTP, sends email to customer
    // =================================
    @Override
    @Transactional
    public void acceptOrder(Long orderId) {

        Long agentId = userService.getCurrentUserId();

        Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Order not available for pickup");
        }

        if (order.getDeliveryAgentId() != null) {
            throw new RuntimeException("Order is already assigned to another agent");
        }

        // Fetch agent details (for email)
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        // Fetch customer details (for email)
        User customer = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Assign agent + update status
        order.setDeliveryAgentId(agentId);
        order.setStatus(OrderStatus.ASSIGNED);
        orderRepo.save(order);

        // Generate OTP and save to delivery_otp table
        String otp = generateOtp();
        DeliveryOtp deliveryOtp = new DeliveryOtp();
        deliveryOtp.setOrderId(orderId);
        deliveryOtp.setOtp(otp);
        deliveryOtpRepository.save(deliveryOtp);

        // Send email to customer (async)
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
    }

    // =================================
    // 🚀 2. START DELIVERY
    //    → status: ASSIGNED → OUT_FOR_DELIVERY
    // =================================
    @Override
    @Transactional
    public void startDelivery(Long orderId) {

        Long agentId = userService.getCurrentUserId();

        Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!agentId.equals(order.getDeliveryAgentId())) {
            throw new RuntimeException("Not your order");
        }

        if (order.getStatus() != OrderStatus.ASSIGNED) {
            throw new RuntimeException("Order must be ASSIGNED before starting delivery");
        }

        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepo.save(order);
    }

    // =================================
    // 📦 3. COMPLETE DELIVERY
    //    → Verifies OTP from customer
    //    → On success: status = DELIVERED
    //    → On failure: increments attempts, locks after 3 wrong tries
    // =================================
    @Override
    @Transactional
    public void completeDelivery(Long orderId, String enteredOtp) {

        Long agentId = userService.getCurrentUserId();

        Order order = orderRepo.findByIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!agentId.equals(order.getDeliveryAgentId())) {
            throw new RuntimeException("Not your order");
        }

        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException("Order is not out for delivery");
        }

        DeliveryOtp deliveryOtp = deliveryOtpRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("OTP not found for this order"));

        if (deliveryOtp.isVerified()) {
            throw new RuntimeException("OTP already used for this order");
        }

        if (deliveryOtp.getAttempts() >= MAX_OTP_ATTEMPTS) {
            throw new RuntimeException("OTP locked after " + MAX_OTP_ATTEMPTS + " failed attempts. Contact support.");
        }

        // ❌ Wrong OTP
        if (!deliveryOtp.getOtp().equals(enteredOtp.trim())) {
            deliveryOtp.setAttempts(deliveryOtp.getAttempts() + 1);
            deliveryOtpRepository.save(deliveryOtp);

            int remaining = MAX_OTP_ATTEMPTS - deliveryOtp.getAttempts();
            if (remaining > 0) {
                throw new RuntimeException("Incorrect OTP. " + remaining + " attempt(s) remaining.");
            } else {
                throw new RuntimeException("Incorrect OTP. OTP is now locked. Please contact support.");
            }
        }

        // ✅ Correct OTP
        deliveryOtp.setVerified(true);
        deliveryOtpRepository.save(deliveryOtp);

        order.setStatus(OrderStatus.DELIVERED);
        orderRepo.save(order);
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