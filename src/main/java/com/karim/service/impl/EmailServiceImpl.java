package com.karim.service.impl;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.karim.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	private JavaMailSender mailSender;

	// =================================
	// SEND SIMPLE OTP EMAIL (Payment OTP)
	// =================================
	@Override
	public void sendOtpEmail(String toEmail, String otp) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(toEmail);
			message.setSubject("Payment OTP Verification");
			message.setText("Dear Customer,\n\n" + "Your OTP for completing the payment is: " + otp + "\n\n"
					+ "This OTP is valid for 5 minutes.\n\n" + "Thank you for shopping with us!\n\n"
					+ "Karim Mart Team");

			mailSender.send(message);

			System.out.println("Payment OTP sent to " + toEmail);

		} catch (Exception e) {
			throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
		}
	}

	// =================================
	// SEND INVOICE EMAIL WITH PDF
	// =================================
	@Override
	public void sendInvoiceEmail(String toEmail, byte[] pdfData) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

			helper.setTo(toEmail);
			helper.setSubject("Payment Successful - Invoice Attached");

			helper.setText("Dear Customer,\n\n" + "Your payment has been successfully completed.\n\n"
					+ "Please find your invoice attached.\n\n" + "Thank you for shopping with us!\n\n"
					+ "Karim Mart Team");

			ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfData, "application/pdf");

			helper.addAttachment("Invoice.pdf", dataSource);

			mailSender.send(mimeMessage);

			System.out.println("Invoice email sent to " + toEmail);

		} catch (MessagingException e) {
			throw new RuntimeException("Failed to send invoice email: " + e.getMessage(), e);
		}
	}

	// =================================
	// SEND FORGOT PASSWORD OTP
	// =================================
	@Override
	public void sendForgotPasswordOtp(String toEmail, String otp) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("Forgot Password OTP");
		message.setText("Your OTP for password reset: " + otp + "\nIt is valid for 5 minutes.");

		mailSender.send(message);
	}

	// =================================
	// GENERIC EMAIL
	// =================================
	public void sendEmail(String to, String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);
		mailSender.send(message);
	}

	// =================================
	// WELCOME EMAIL
	// =================================
	@Override
	public void sendWelcomeEmail(String to, String username) {

		String subject = "Welcome to Karim Mart!";
		String body = "Hello " + username + ",\n\n" + "Welcome to Karim Mart! We're excited to have you on board.\n\n"
				+ "Happy Shopping!\n\n" + "Regards,\nKarim Mart Team";

		sendEmail(to, subject, body);
	}
}
