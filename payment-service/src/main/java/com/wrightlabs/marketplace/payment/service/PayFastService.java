package com.wrightlabs.marketplace.payment.service;

import com.wrightlabs.marketplace.domain.entity.Order;
import com.wrightlabs.marketplace.domain.repository.OrderRepository;
import com.wrightlabs.marketplace.payment.config.PayFastProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayFastService {

    private final PayFastProperties payFastProperties;
    private final OrderRepository orderRepository;

    /**
     * Generate PayFast payment form data for an order
     */
    public Map<String, String> generatePaymentData(Order order) {
        Map<String, String> data = new LinkedHashMap<>();

        data.put("merchant_id", payFastProperties.getMerchantId());
        data.put("merchant_key", payFastProperties.getMerchantKey());
        data.put("return_url", payFastProperties.getReturnUrl());
        data.put("cancel_url", payFastProperties.getCancelUrl());
        data.put("notify_url", payFastProperties.getNotifyUrl());

        data.put("name_first", order.getBuyerName() != null ? order.getBuyerName() : "Buyer");
        data.put("email_address", order.getBuyerEmail());

        data.put("m_payment_id", order.getOrderNumber());
        data.put("amount", order.getTotalAmount().toString());
        data.put("item_name", "Order " + order.getOrderNumber());
        data.put("item_description", "Lesson resources purchase");

        // Generate signature
        String signature = generateSignature(data, payFastProperties.getPassphrase());
        data.put("signature", signature);

        log.info("Generated PayFast payment data for order: {}", order.getOrderNumber());
        return data;
    }

    /**
     * Generate MD5 signature for PayFast
     */
    private String generateSignature(Map<String, String> data, String passphrase) {
        String paramString = data.entrySet().stream()
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));

        if (passphrase != null && !passphrase.isEmpty()) {
            paramString += "&passphrase=" + urlEncode(passphrase);
        }

        return DigestUtils.md5Hex(paramString);
    }

    /**
     * Validate PayFast IPN (Instant Payment Notification) signature
     */
    public boolean validateSignature(Map<String, String> postData) {
        String receivedSignature = postData.get("signature");

        // Remove signature from data for validation
        Map<String, String> dataToValidate = new LinkedHashMap<>(postData);
        dataToValidate.remove("signature");

        String generatedSignature = generateSignature(dataToValidate, payFastProperties.getPassphrase());

        boolean valid = generatedSignature.equals(receivedSignature);
        log.info("PayFast signature validation result: {}", valid);
        return valid;
    }

    /**
     * Process PayFast IPN notification
     */
    @Transactional
    public void processPaymentNotification(Map<String, String> postData) {
        if (!validateSignature(postData)) {
            log.error("Invalid PayFast signature");
            throw new IllegalArgumentException("Invalid PayFast signature");
        }

        String orderNumber = postData.get("m_payment_id");
        String paymentStatus = postData.get("payment_status");
        String payFastPaymentId = postData.get("pf_payment_id");

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));

        log.info("Processing PayFast notification for order: {}, status: {}", orderNumber, paymentStatus);

        if ("COMPLETE".equalsIgnoreCase(paymentStatus)) {
            order.setStatus(Order.OrderStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
            order.setPaymentProviderRef(payFastPaymentId);
            orderRepository.save(order);

            // TODO: Trigger digital delivery (will be handled by event listener)
            log.info("Order {} marked as PAID", orderNumber);
        } else {
            order.setStatus(Order.OrderStatus.FAILED);
            orderRepository.save(order);
            log.warn("Order {} payment failed or was cancelled", orderNumber);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public String getPaymentUrl() {
        return payFastProperties.getBaseUrl() + "/eng/process";
    }
}
