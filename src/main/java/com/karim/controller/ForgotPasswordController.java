package com.karim.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karim.dto.ForgotPasswordRequest;
import com.karim.dto.ResetPasswordRequest;
import com.karim.service.ForgotPasswordService;

@RestController
@RequestMapping("/api/auth")
public class ForgotPasswordController {

	@Autowired
	private ForgotPasswordService forgotPasswordService;

	// ================================
	// REQUEST PASSWORD RESET
	// ================================
	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {

		forgotPasswordService.requestPasswordReset(request);

		return ResponseEntity.ok("OTP sent to your registered email");
	}

	// ================================
	// RESET PASSWORD
	// ================================
	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {

		forgotPasswordService.resetPassword(request);

		return ResponseEntity.ok("Password reset successfully");
	}
}
