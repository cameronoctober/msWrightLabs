package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.catalog.dto.ReviewRequest;
import com.wrightlabs.marketplace.catalog.service.ReviewService;
import com.wrightlabs.marketplace.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final com.wrightlabs.marketplace.auth.service.UserService userService;

    @PostMapping
    public String submitReview(@Valid @ModelAttribute ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
            reviewService.submitReview(request, user);

            redirectAttributes.addFlashAttribute("success", "Review submitted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/products/" + request.getProductId();
    }
}
