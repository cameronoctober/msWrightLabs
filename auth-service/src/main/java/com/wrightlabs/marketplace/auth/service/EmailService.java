package com.wrightlabs.marketplace.auth.service;

import com.wrightlabs.marketplace.domain.entity.Order;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@lessonmarketplace.com}")
    private String fromEmail;

    @Value("${platform.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendOrderConfirmation(Order order) {
        try {
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/order-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getBuyerEmail());
            helper.setSubject("Order Confirmation - " + order.getOrderNumber());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent order confirmation email to: {}", order.getBuyerEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email", e);
        }
    }

    public void sendPasswordReset(String email, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("resetUrl", baseUrl + "/reset-password?token=" + resetToken);
            context.setVariable("email", email);

            String htmlContent = templateEngine.process("emails/password-reset", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent password reset email to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email", e);
        }
    }

    public void sendEmailVerification(String email, String verificationToken) {
        try {
            Context context = new Context();
            context.setVariable("verificationUrl", baseUrl + "/verify-email?token=" + verificationToken);
            context.setVariable("email", email);

            String htmlContent = templateEngine.process("emails/email-verification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Verify Your Email Address");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Sent email verification to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send email verification", e);
        }
    }
}
