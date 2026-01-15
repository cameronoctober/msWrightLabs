package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.User;
import com.wrightlabs.marketplace.payment.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final com.wrightlabs.marketplace.auth.service.UserService userService;

    @GetMapping
    public String viewWishlist(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        List<Product> wishlist = wishlistService.getUserWishlist(user.getId());

        model.addAttribute("wishlist", wishlist);
        return "wishlist/index";
    }

    @PostMapping("/add")
    public String addToWishlist(@RequestParam Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
            wishlistService.addToWishlist(productId, user);

            redirectAttributes.addFlashAttribute("success", "Added to wishlist");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/products/" + productId;
    }

    @PostMapping("/remove")
    public String removeFromWishlist(@RequestParam Long productId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
            wishlistService.removeFromWishlist(productId, user);

            redirectAttributes.addFlashAttribute("success", "Removed from wishlist");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/wishlist";
    }
}
