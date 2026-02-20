package com.karim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karim.dto.OtpVerifyRequest;
import com.karim.dto.PaymentRequest;
import com.karim.service.PaymentService;
import com.karim.service.UserService;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private UserService userService;

	// =====================
	// INITIATE PAYMENT
	// =====================
	@PostMapping("/pay")
	public ResponseEntity<String> pay(@RequestBody PaymentRequest request) {

		Long userId = userService.getCurrentUserId();

		paymentService.initiatePayment(userId, request);

		return ResponseEntity.ok("OTP sent to your email");
	}

	// =====================
	// VERIFY OTP
	// =====================
	@PostMapping("/verify-otp")
	public ResponseEntity<String> verifyOtp(@RequestBody OtpVerifyRequest request) {

		Long userId = userService.getCurrentUserId();

		paymentService.verifyOtp(userId, request);

		return ResponseEntity.ok("Payment successful!");
	}
}
