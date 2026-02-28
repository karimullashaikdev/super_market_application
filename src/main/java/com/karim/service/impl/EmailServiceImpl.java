//package com.karim.service.impl;
//
//import java.nio.charset.StandardCharsets;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import com.karim.service.EmailService;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import jakarta.mail.util.ByteArrayDataSource;
//
//@Service
//public class EmailServiceImpl implements EmailService {
//
//	@Autowired
//	private JavaMailSender mailSender;
//
//	// =================================
//	// SEND SIMPLE OTP EMAIL (Payment OTP)
//	// =================================
//	@Override
//	public void sendOtpEmail(String toEmail, String otp) {
//		try {
//			SimpleMailMessage message = new SimpleMailMessage();
//			message.setTo(toEmail);
//			message.setSubject("Payment OTP Verification");
//			message.setText("Dear Customer,\n\n" + "Your OTP for completing the payment is: " + otp + "\n\n"
//					+ "This OTP is valid for 5 minutes.\n\n" + "Thank you for shopping with us!\n\n"
//					+ "Karim Mart Team");
//
//			mailSender.send(message);
//
//			System.out.println("Payment OTP sent to " + toEmail);
//
//		} catch (Exception e) {
//			throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
//		}
//	}
//
//	// =================================
//	// SEND INVOICE EMAIL WITH PDF
//	// =================================
//	@Override
//	public void sendInvoiceEmail(String toEmail, byte[] pdfData) {
//		try {
//			MimeMessage mimeMessage = mailSender.createMimeMessage();
//
//			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
//
//			helper.setTo(toEmail);
//			helper.setSubject("Payment Successful - Invoice Attached");
//
//			helper.setText("Dear Customer,\n\n" + "Your payment has been successfully completed.\n\n"
//					+ "Please find your invoice attached.\n\n" + "Thank you for shopping with us!\n\n"
//					+ "Karim Mart Team");
//
//			ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfData, "application/pdf");
//
//			helper.addAttachment("Invoice.pdf", dataSource);
//
//			mailSender.send(mimeMessage);
//
//			System.out.println("Invoice email sent to " + toEmail);
//
//		} catch (MessagingException e) {
//			throw new RuntimeException("Failed to send invoice email: " + e.getMessage(), e);
//		}
//	}
//
//	// =================================
//	// SEND FORGOT PASSWORD OTP
//	// =================================
//	@Override
//	public void sendForgotPasswordOtp(String toEmail, String otp) {
//		SimpleMailMessage message = new SimpleMailMessage();
//		message.setTo(toEmail);
//		message.setSubject("Forgot Password OTP");
//		message.setText("Your OTP for password reset: " + otp + "\nIt is valid for 5 minutes.");
//
//		mailSender.send(message);
//	}
//
//	// =================================
//	// GENERIC EMAIL
//	// =================================
//	public void sendEmail(String to, String subject, String body) {
//		SimpleMailMessage message = new SimpleMailMessage();
//		message.setTo(to);
//		message.setSubject(subject);
//		message.setText(body);
//		mailSender.send(message);
//	}
//
//	// =================================
//	// WELCOME EMAIL
//	// =================================
//	@Override
//	public void sendWelcomeEmail(String to, String username) {
//
//		String subject = "Welcome to Karim Mart!";
//		String body = "Hello " + username + ",\n\n" + "Welcome to Karim Mart! We're excited to have you on board.\n\n"
//				+ "Happy Shopping!\n\n" + "Regards,\nKarim Mart Team";
//
//		sendEmail(to, subject, body);
//	}
//}

package com.karim.service.impl;

import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.karim.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    // =================================
    // COMMON METHOD TO SEND EMAIL
    // =================================
    private void sendBrevoEmail(String toEmail, String subject, String textContent, byte[] pdfAttachment) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"sender\":{\"name\":\"").append(senderName).append("\",\"email\":\"").append(senderEmail).append("\"},");
            json.append("\"to\":[{\"email\":\"").append(toEmail).append("\"}],");
            json.append("\"subject\":\"").append(subject).append("\",");
            json.append("\"textContent\":\"").append(textContent.replace("\n", "\\n")).append("\"");

            // Attach PDF if provided
            if (pdfAttachment != null) {
                String base64Pdf = Base64.getEncoder().encodeToString(pdfAttachment);
                json.append(",\"attachment\":[{\"content\":\"").append(base64Pdf).append("\",\"name\":\"Invoice.pdf\"}]");
            }

            json.append("}");

            HttpEntity<String> request = new HttpEntity<>(json.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, request, String.class);

            System.out.println("Email sent to " + toEmail + " | Status: " + response.getStatusCode());

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email via Brevo: " + e.getMessage(), e);
        }
    }

    // =================================
    // SEND SIMPLE OTP EMAIL (Payment OTP)
    // =================================
    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "Payment OTP Verification";
        String body = "Dear Customer,\n\n"
                + "Your OTP for completing the payment is: " + otp + "\n\n"
                + "This OTP is valid for 5 minutes.\n\n"
                + "Thank you for shopping with us!\n\n"
                + "Karim Mart Team";
        sendBrevoEmail(toEmail, subject, body, null);
    }

    // =================================
    // SEND INVOICE EMAIL WITH PDF
    // =================================
    @Override
    public void sendInvoiceEmail(String toEmail, byte[] pdfData) {
        String subject = "Payment Successful - Invoice Attached";
        String body = "Dear Customer,\n\n"
                + "Your payment has been successfully completed.\n\n"
                + "Please find your invoice attached.\n\n"
                + "Thank you for shopping with us!\n\n"
                + "Karim Mart Team";
        sendBrevoEmail(toEmail, subject, body, pdfData);
    }

    // =================================
    // SEND FORGOT PASSWORD OTP
    // =================================
    @Override
    public void sendForgotPasswordOtp(String toEmail, String otp) {
        String subject = "Forgot Password OTP";
        String body = "Your OTP for password reset: " + otp + "\nIt is valid for 5 minutes.";
        sendBrevoEmail(toEmail, subject, body, null);
    }

    // =================================
    // GENERIC EMAIL
    // =================================
    public void sendEmail(String to, String subject, String body) {
        sendBrevoEmail(to, subject, body, null);
    }

    // =================================
    // WELCOME EMAIL
    // =================================
    @Override
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to Karim Mart!";
        String body = "Hello " + username + ",\n\n"
                + "Welcome to Karim Mart! We're excited to have you on board.\n\n"
                + "Happy Shopping!\n\n"
                + "Regards,\nKarim Mart Team";
        sendBrevoEmail(to, subject, body, null);
    }
}
