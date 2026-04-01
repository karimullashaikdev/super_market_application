package com.karim.service;

public interface EmailService {

	void sendOtpEmail(String toEmail, String otp);

	void sendInvoiceEmail(String toEmail, byte[] pdfData);

	void sendForgotPasswordOtp(String toEmail, String otp);

	void sendWelcomeEmail(String to, String username);
	
	// ── NEW: Delivery assignment email with agent details + OTP + tracking link ──
    void sendDeliveryAssignedEmail(
            String toEmail,
            String customerName,
            Long orderId,
            Double totalAmount,
            String paymentType,
            String deliveryAddress,
            String agentId,
            String agentName,
            String agentMobile,
            String otp
    );
}
