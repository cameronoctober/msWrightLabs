package com.wrightlabs.marketplace.payment.service;

import com.wrightlabs.marketplace.domain.entity.*;
import com.wrightlabs.marketplace.domain.repository.OrderItemRepository;
import com.wrightlabs.marketplace.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Value("${platform.commission-rate:0.15}")
    private Double commissionRate;

    @Transactional
    public Order createOrderFromCart(Cart cart, User buyer) {
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Calculate total
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        String orderNumber = generateOrderNumber();
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .buyer(buyer)
                .buyerEmail(buyer != null ? buyer.getEmail() : "guest@example.com")
                .buyerName(buyer != null ? buyer.getEmail() : "Guest")
                .totalAmount(total)
                .currency("ZAR")
                .status(Order.OrderStatus.PENDING)
                .paymentProvider("PayFast")
                .build();

        order = orderRepository.save(order);
        log.info("Cre ated order: {}", orderNumber);

        // Create order items with revenue split
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            BigDecimal price = product.getPrice();
            BigDecimal platformFee = price.multiply(BigDecimal.valueOf(commissionRate))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal sellerAmount = price.subtract(platformFee);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .seller(product.getSeller())
                    .productTitle(product.getTitle())
                    .price(price)
                    .platformFee(platformFee)
                    .sellerAmount(sellerAmount)
                    .quantity(1)
                    .build();

            orderItemRepository.save(orderItem);
        }

        return order;
    }

    @Transactional
    public void markOrderAsPaid(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setStatus(Order.OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order {} marked as PAID", orderNumber);

        // TODO: Trigger event for digital delivery (increment product purchases)
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByBuyerId(userId, pageable);
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    private String generateOrderNumber() {
        // Format: ORD-YYYYMMDD-UUID(8 chars)
        String datePart = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("ORD-%s-%s", datePart, uuidPart);
    }
}
