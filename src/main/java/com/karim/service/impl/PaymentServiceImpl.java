package com.karim.service.impl;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karim.dto.OtpVerifyRequest;
import com.karim.dto.PaymentRequest;
import com.karim.entity.Order;
import com.karim.entity.PaymentOtp;
import com.karim.enums.OrderStatus;
import com.karim.enums.PaymentStatus;
import com.karim.exception.OrderNotFoundException;
import com.karim.exception.UnauthorizedException;
import com.karim.repository.CartItemRepository;
import com.karim.repository.OrderRepository;
import com.karim.repository.PaymentOtpRepository;
import com.karim.service.EmailService;
import com.karim.service.PaymentService;
import com.karim.service.UserService;
import com.karim.util.PdfUtil;

@Service
public class PaymentServiceImpl implements PaymentService {

	@Autowired
	private OrderRepository orderRepo;

	@Autowired
	private PaymentOtpRepository otpRepo;

	@Autowired
	private CartItemRepository cartRepo;

	@Autowired
	private UserService userService;

	@Autowired
	private EmailService emailService;

	// =================================
	// INITIATE PAYMENT (GENERATE OTP)
	// =================================
	@Override
	@Transactional
	public void initiatePayment(Long userId, PaymentRequest request) {

		Order order = orderRepo.findByIdAndDeletedFalse(request.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException("Order not found"));

		if (!order.getUserId().equals(userId)) {
			throw new UnauthorizedException("Access denied for this order");
		}

		// ✅ FIXED (CONFIRMED → PAID)
		if (order.getStatus() == OrderStatus.PAID) {
			throw new RuntimeException("Order is already paid");
		}

		PaymentOtp existingOtp = otpRepo.findByOrderIdAndDeletedFalse(order.getId()).orElse(null);

		String otp;

		if (existingOtp != null && !existingOtp.isVerified() && existingOtp.getExpiry().isAfter(LocalDateTime.now())) {

			otp = existingOtp.getOtp();

		} else {

			otp = generateOtp();

			PaymentOtp paymentOtp = new PaymentOtp();
			paymentOtp.setOrderId(order.getId());
			paymentOtp.setOtp(otp);
			paymentOtp.setExpiry(LocalDateTime.now().plusMinutes(5));
			paymentOtp.setVerified(false);
			paymentOtp.setDeleted(false);

			otpRepo.save(paymentOtp);
		}

		String email = userService.getCurrentUserEmail();
		emailService.sendOtpEmail(email, otp);

		System.out.println("OTP sent to " + email + " for Order ID: " + order.getId());
	}

	// =================================
	// VERIFY OTP
	// =================================
	@Override
	@Transactional
	public void verifyOtp(Long userId, OtpVerifyRequest request) {

		PaymentOtp otp = otpRepo.findByOrderIdAndDeletedFalse(request.getOrderId())
				.orElseThrow(() -> new RuntimeException("OTP not found"));

		Order order = orderRepo.findByIdAndDeletedFalse(request.getOrderId())
				.orElseThrow(() -> new OrderNotFoundException("Order not found"));

		if (!order.getUserId().equals(userId)) {
			throw new UnauthorizedException("Access denied");
		}

		if (otp.isVerified()) {
			throw new RuntimeException("OTP already verified");
		}

		if (otp.getExpiry().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("OTP expired");
		}

		if (!otp.getOtp().equals(request.getOtp())) {
			throw new RuntimeException("Invalid OTP");
		}

		// ✅ Mark OTP verified
		otp.setVerified(true);
		otpRepo.save(otp);

		// ✅ FIXED STATUS FLOW
		order.setStatus(OrderStatus.PAID);

		// ✅ ADD PAYMENT STATUS
		order.setPaymentStatus(PaymentStatus.SUCCESS);

		orderRepo.save(order);

		// 🔥 Generate Invoice
		byte[] pdf = PdfUtil.generateBill(order);

		String email = userService.getCurrentUserEmail();

		emailService.sendInvoiceEmail(email, pdf);

		System.out.println("Payment successful. Invoice sent for Order ID: " + order.getId());
	}

	// =================================
	// HELPER
	// =================================
	private String generateOtp() {
		return String.valueOf(new Random().nextInt(900000) + 100000);
	}
}