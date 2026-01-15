package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.auth.service.EmailService;
import com.wrightlabs.marketplace.catalog.service.ProductService;
import com.wrightlabs.marketplace.domain.entity.Order;
import com.wrightlabs.marketplace.payment.service.CartService;
import com.wrightlabs.marketplace.payment.service.OrderService;
import com.wrightlabs.marketplace.payment.service.PayFastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class PayFastWebhookController {

    private final PayFastService payFastService;
    private final OrderService orderService;
    private final CartService cartService;
    private final ProductService productService;
    private final EmailService emailService;

    @PostMapping("/payfast")
    public String handlePayFastIPN(@RequestParam Map<String, String> params) {
        try {
            // Validate signature
            if (!payFastService.validateSignature(params)) {
                log.error("Invalid PayFast signature");
                return "Invalid signature";
            }

            // Process payment notification
            payFastService.processPaymentNotification(params);

            String orderNumber = params.get("m_payment_id");
            Order order = orderService.getOrderByNumber(orderNumber);

            // Increment purchase count for all products in the order
            order.getItems().forEach(item -> {
                if (item.getProduct() != null) {
                    productService.incrementPurchaseCount(item.getProduct().getId());
                }
            });

            // Send order confirmation email
            emailService.sendOrderConfirmation(order);

            log.info("PayFast IPN processed successfully for order: {}", orderNumber);
            return "Success";
        } catch (Exception e) {
            log.error("PayFast IPN processing failed", e);
            return "Error: " + e.getMessage();
        }
    }
}
