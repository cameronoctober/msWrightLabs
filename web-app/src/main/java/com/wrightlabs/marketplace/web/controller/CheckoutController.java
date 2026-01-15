package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.auth.service.EmailService;
import com.wrightlabs.marketplace.domain.entity.Cart;
import com.wrightlabs.marketplace.domain.entity.Order;
import com.wrightlabs.marketplace.domain.entity.User;
import com.wrightlabs.marketplace.payment.service.CartService;
import com.wrightlabs.marketplace.payment.service.OrderService;
import com.wrightlabs.marketplace.payment.service.PayFastService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PayFastService payFastService;
    private final EmailService emailService;
    private final com.wrightlabs.marketplace.auth.service.UserService userService;

    @GetMapping
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        String sessionId = session.getId();
        Cart cart = cartService.getOrCreateCart(user, sessionId);

        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // Create order
        Order order = orderService.createOrderFromCart(cart, user);

        // Generate PayFast payment data
        Map<String, String> paymentData = payFastService.generatePaymentData(order);
        String paymentUrl = payFastService.getPaymentUrl();

        model.addAttribute("order", order);
        model.addAttribute("paymentData", paymentData);
        model.addAttribute("paymentUrl", paymentUrl);

        return "checkout/summary";
    }

    @GetMapping("/success")
    public String checkoutSuccess(@RequestParam(required = false) String m_payment_id, Model model) {
        if (m_payment_id != null) {
            try {
                Order order = orderService.getOrderByNumber(m_payment_id);
                model.addAttribute("order", order);
            } catch (Exception e) {
                // Order might not be marked as paid yet if IPN hasn't been processed
            }
        }

        model.addAttribute("message", "Thank you for your purchase! You will receive a confirmation email shortly.");
        return "checkout/success";
    }

    @GetMapping("/cancel")
    public String checkoutCancel(Model model) {
        model.addAttribute("message", "Payment was cancelled. You can try again from your cart.");
        return "checkout/cancel";
    }
}
