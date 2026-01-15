package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.domain.entity.Cart;
import com.wrightlabs.marketplace.domain.entity.User;
import com.wrightlabs.marketplace.payment.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final com.wrightlabs.marketplace.auth.service.UserService userService;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            Model model) {
        User user = userDetails != null ? userService.findByEmail(userDetails.getUsername()).orElse(null) : null;

        String sessionId = session.getId();
        Cart cart = cartService.getOrCreateCart(user, sessionId);

        BigDecimal total = cartService.calculateCartTotal(cart);

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.getItems());
        model.addAttribute("total", total);

        return "cart/view";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userDetails != null ? userService.findByEmail(userDetails.getUsername()).orElse(null) : null;

            String sessionId = session.getId();
            cartService.addToCart(productId, user, sessionId);

            redirectAttributes.addFlashAttribute("success", "Product added to cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/products/" + productId;
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long cartItemId,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.removeFromCart(cartItemId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }
}
