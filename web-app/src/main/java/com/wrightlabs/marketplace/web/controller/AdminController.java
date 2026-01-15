package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.admin.service.AdminService;
import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.Review;
import com.wrightlabs.marketplace.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalRevenue", adminService.getTotalRevenue());
        model.addAttribute("totalOrders", adminService.getTotalOrders());
        model.addAttribute("totalProducts", adminService.getTotalProducts());
        model.addAttribute("totalUsers", adminService.getTotalUsers());
        model.addAttribute("recentOrders", adminService.getRecentOrders(10));

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<User> users = adminService.getAllUsers(PageRequest.of(page, 20));

        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/{id}/lock")
    public String lockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.lockUser(id);
            redirectAttributes.addFlashAttribute("success", "User locked successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unlock")
    public String unlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.unlockUser(id);
            redirectAttributes.addFlashAttribute("success", "User unlocked successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/products")
    public String products(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Product> products = adminService.getAllProducts(PageRequest.of(page, 20));

        model.addAttribute("products", products);
        return "admin/products";
    }

    @PostMapping("/products/{id}/approve")
    public String approveProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.approveProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/archive")
    public String archiveProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.archiveProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product archived successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/reviews")
    public String reviews(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Review> reviews = adminService.getAllReviews(PageRequest.of(page, 20));

        model.addAttribute("reviews", reviews);
        return "admin/reviews";
    }

    @PostMapping("/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.approveReview(id);
            redirectAttributes.addFlashAttribute("success", "Review approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/reviews";
    }

    @PostMapping("/reviews/{id}/reject")
    public String rejectReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.rejectReview(id);
            redirectAttributes.addFlashAttribute("success", "Review rejected successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/reviews";
    }
}
