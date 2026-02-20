package com.karim.service;

public interface EmailService {

	void sendOtpEmail(String toEmail, String otp);

	void sendInvoiceEmail(String toEmail, byte[] pdfData);

	void sendForgotPasswordOtp(String toEmail, String otp);

	void sendWelcomeEmail(String to, String username);
}
