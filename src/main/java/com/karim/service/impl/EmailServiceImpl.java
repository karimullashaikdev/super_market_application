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
import org.springframework.scheduling.annotation.Async;
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

    // ✅ NEW — inject base URL from active profile (dev/prod)
    // In application-dev.properties  → app.base-url=http://localhost:5500
    // In application-prod.properties → app.base-url=https://yourdomain.com
    @Value("${app.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    // ==========================================================
    // COMMON SEND METHOD (existing — unchanged)
    // ==========================================================
    private void sendBrevoEmail(String toEmail, String subject, String htmlContent, byte[] pdfAttachment) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Escape for JSON string
            String safeHtml = htmlContent
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"sender\":{\"name\":\"").append(senderName).append("\",\"email\":\"").append(senderEmail).append("\"},");
            json.append("\"to\":[{\"email\":\"").append(toEmail).append("\"}],");
            json.append("\"subject\":\"").append(subject).append("\",");
            json.append("\"htmlContent\":\"").append(safeHtml).append("\"");

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

    // ==========================================================
    // EXISTING METHODS — unchanged
    // ==========================================================
    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "Payment OTP Verification";
        String body = "Dear Customer,<br><br>Your OTP for completing the payment is: <b>" + otp + "</b><br><br>"
                + "This OTP is valid for 5 minutes.<br><br>Thank you for shopping with us!<br><br>Karim Mart Team";
        sendBrevoEmail(toEmail, subject, body, null);
    }

    @Override
    public void sendInvoiceEmail(String toEmail, byte[] pdfData) {
        String subject = "Payment Successful - Invoice Attached";
        String body = "Dear Customer,<br><br>Your payment has been successfully completed.<br><br>"
                + "Please find your invoice attached.<br><br>Thank you for shopping with us!<br><br>Karim Mart Team";
        sendBrevoEmail(toEmail, subject, body, pdfData);
    }

    @Override
    public void sendForgotPasswordOtp(String toEmail, String otp) {
        String subject = "Forgot Password OTP";
        String body = "Your OTP for password reset: <b>" + otp + "</b><br>It is valid for 5 minutes.";
        sendBrevoEmail(toEmail, subject, body, null);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to Karim Mart!";
        String body = "Hello <b>" + username + "</b>,<br><br>Welcome to Karim Mart! We're excited to have you on board.<br><br>"
                + "Happy Shopping!<br><br>Regards,<br>Karim Mart Team";
        sendBrevoEmail(to, subject, body, null);
    }

    // ==========================================================
    // ✅ NEW — Delivery Assigned Email
    // Called when delivery agent accepts an order
    // ==========================================================
    @Override
    @Async
    public void sendDeliveryAssignedEmail(
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
    ) {
        String subject = "Your Order #" + orderId + " is On the Way! 🛵";

        // Tracking link — baseUrl comes from active profile (dev/prod)
        String trackingLink = baseUrl + "/tracking.html?orderId=" + orderId;

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                + "<style>"
                + "body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}"
                + ".wrap{max-width:600px;margin:30px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08)}"
                + ".header{background:linear-gradient(135deg,#f5a623,#e8522a);padding:28px 32px;text-align:center}"
                + ".header h1{color:#fff;margin:0;font-size:22px;font-weight:700}"
                + ".header p{color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:14px}"
                + ".body{padding:28px 32px}"
                + ".greeting{font-size:15px;color:#333;margin-bottom:20px;line-height:1.6}"
                + ".section{background:#f8f9fb;border:1px solid #e8eaf0;border-radius:10px;padding:18px 20px;margin-bottom:16px}"
                + ".section-title{font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:0.08em;color:#888;margin-bottom:12px}"
                + ".row{display:flex;justify-content:space-between;font-size:13px;margin-bottom:8px;gap:12px}"
                + ".row:last-child{margin-bottom:0}"
                + ".row .label{color:#888;min-width:110px}"
                + ".row .val{color:#222;font-weight:600;text-align:right}"
                + ".track-btn{display:block;margin:20px 0;padding:14px;background:linear-gradient(135deg,#3b82f6,#2563eb);color:#fff;text-align:center;text-decoration:none;border-radius:10px;font-size:15px;font-weight:700}"
                + ".otp-box{background:linear-gradient(135deg,#fff8ec,#fff3e0);border:2px dashed #f5a623;border-radius:10px;padding:20px;text-align:center;margin-top:16px}"
                + ".otp-label{font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:0.1em;color:#f5a623;margin-bottom:8px}"
                + ".otp-value{font-size:38px;font-weight:800;letter-spacing:10px;color:#1a1a1a}"
                + ".otp-hint{font-size:12px;color:#888;margin-top:8px}"
                + ".footer{background:#f8f9fb;padding:18px 32px;text-align:center;font-size:12px;color:#aaa;border-top:1px solid #eee}"
                + "</style></head><body>"
                + "<div class='wrap'>"

                // Header
                + "<div class='header'>"
                + "<h1>🛵 Your Order is On the Way!</h1>"
                + "<p>Order #" + orderId + " has been assigned to a delivery agent</p>"
                + "</div>"

                // Body
                + "<div class='body'>"
                + "<p class='greeting'>Hi <strong>" + customerName + "</strong>,<br>"
                + "Great news! Your order has been picked up by a delivery agent and is heading your way.</p>"

                // Agent Details
                + "<div class='section'>"
                + "<div class='section-title'>🛵 Delivery Agent</div>"
                + "<div class='row'><span class='label'>Agent ID</span><span class='val'>" + agentId + "</span></div>"
                + "<div class='row'><span class='label'>Name</span><span class='val'>" + agentName + "</span></div>"
                + "<div class='row'><span class='label'>Mobile</span><span class='val'>" + agentMobile + "</span></div>"
                + "</div>"

                // Order Details
                + "<div class='section'>"
                + "<div class='section-title'>📦 Order Details</div>"
                + "<div class='row'><span class='label'>Order #</span><span class='val'>" + orderId + "</span></div>"
                + "<div class='row'><span class='label'>Amount</span><span class='val'>₹" + String.format("%.2f", totalAmount) + "</span></div>"
                + "<div class='row'><span class='label'>Payment</span><span class='val'>" + paymentType + "</span></div>"
                + "<div class='row'><span class='label'>Address</span><span class='val'>" + deliveryAddress + "</span></div>"
                + "</div>"

                // Tracking Button
                + "<a href='" + trackingLink + "' class='track-btn'>📍 Track Your Order Live — Click Here</a>"

                // OTP Box
                + "<div class='otp-box'>"
                + "<div class='otp-label'>🔐 Delivery OTP — Share only with your agent</div>"
                + "<div class='otp-value'>" + otp + "</div>"
                + "<div class='otp-hint'>The delivery agent will ask for this 4-digit code when handing over your order.<br><strong>Do not share this with anyone else.</strong></div>"
                + "</div>"

                + "</div>" // end body

                // Footer
                + "<div class='footer'>"
                + "For support, contact us at support@karim.com<br>"
                + "Thank you for shopping with <strong>Karim Mart</strong> 🙏"
                + "</div>"

                + "</div></body></html>";

        sendBrevoEmail(toEmail, subject, html, null);
    }
}
