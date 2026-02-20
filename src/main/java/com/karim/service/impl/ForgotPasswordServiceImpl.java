package com.karim.service.impl;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karim.dto.ForgotPasswordRequest;
import com.karim.dto.ResetPasswordRequest;
import com.karim.entity.PasswordResetOtp;
import com.karim.entity.User;
import com.karim.exception.UserNotFoundException;
import com.karim.repository.PasswordResetOtpRepository;
import com.karim.repository.UserRepository;
import com.karim.service.EmailService;
import com.karim.service.ForgotPasswordService;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private PasswordResetOtpRepository otpRepo;

	@Autowired
	private EmailService emailService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// ================================
	// REQUEST PASSWORD RESET
	// ================================
	@Override
	@Transactional
	public void requestPasswordReset(ForgotPasswordRequest request) {

		User user = userRepo.findByEmail(request.getEmail())
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		// Check existing OTP
		PasswordResetOtp existingOtp = otpRepo.findByUserIdAndDeletedFalse(user.getId()).orElse(null);

		String otp;
		if (existingOtp != null && !existingOtp.isVerified() && existingOtp.getExpiry().isAfter(LocalDateTime.now())) {
			otp = existingOtp.getOtp(); // reuse OTP
		} else {
			otp = generateOtp();

			PasswordResetOtp resetOtp = new PasswordResetOtp();
			resetOtp.setUserId(user.getId());
			resetOtp.setOtp(otp);
			resetOtp.setExpiry(LocalDateTime.now().plusMinutes(5));
			resetOtp.setVerified(false);
			resetOtp.setDeleted(false);

			otpRepo.save(resetOtp);
		}

		// Send email with OTP
		emailService.sendForgotPasswordOtp(user.getEmail(), otp);

		System.out.println("Password reset OTP sent to " + user.getEmail());
	}

	// ================================
	// RESET PASSWORD
	// ================================
	@Override
	@Transactional
	public void resetPassword(ResetPasswordRequest request) {

		User user = userRepo.findByEmail(request.getEmail())
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		PasswordResetOtp otp = otpRepo.findByUserIdAndDeletedFalse(user.getId())
				.orElseThrow(() -> new RuntimeException("OTP not found"));

		if (otp.isVerified()) {
			throw new RuntimeException("OTP already used");
		}

		if (otp.getExpiry().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("OTP expired");
		}

		if (!otp.getOtp().equals(request.getOtp())) {
			throw new RuntimeException("Invalid OTP");
		}

		// Mark OTP verified
		otp.setVerified(true);
		otpRepo.save(otp);

		// Update password
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepo.save(user);

		System.out.println("Password successfully reset for " + user.getEmail());
	}

	// ================================
	// HELPER
	// ================================
	private String generateOtp() {
		return String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit
	}
}
