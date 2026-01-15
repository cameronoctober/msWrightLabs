package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.auth.dto.ForgotPasswordRequest;
import com.wrightlabs.marketplace.auth.dto.RegisterRequest;
import com.wrightlabs.marketplace.auth.dto.ResetPasswordRequest;
import com.wrightlabs.marketplace.auth.service.EmailService;
import com.wrightlabs.marketplace.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerUser(request);
            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please check your email to verify your account.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "auth/login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("request", new ForgotPasswordRequest());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("request") ForgotPasswordRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            String token = userService.generatePasswordResetToken(request.getEmail());
            emailService.sendPasswordReset(request.getEmail(), token);
            redirectAttributes.addFlashAttribute("success",
                    "Password reset instructions have been sent to your email");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Password reset failed", e);
            model.addAttribute("error", "Email address not found");
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        model.addAttribute("request", request);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("request") ResetPasswordRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/reset-password";
        }

        if (!request.isPasswordMatching()) {
            model.addAttribute("error", "Passwords do not match");
            return "auth/reset-password";
        }

        try {
            userService.resetPassword(request.getToken(), request.getPassword());
            redirectAttributes.addFlashAttribute("success",
                    "Password reset successful! Please login with your new password");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Password reset failed", e);
            model.addAttribute("error", e.getMessage());
            return "auth/reset-password";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token, Model model) {
        try {
            userService.verifyEmail(token);
            model.addAttribute("success", true);
            model.addAttribute("message", "Email verified successfully! You can now login");
        } catch (Exception e) {
            log.error("Email verification failed", e);
            model.addAttribute("success", false);
            model.addAttribute("message", e.getMessage());
        }
        return "auth/verify-email";
    }
}
