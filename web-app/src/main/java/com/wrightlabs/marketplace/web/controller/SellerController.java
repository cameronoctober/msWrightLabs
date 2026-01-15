package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.catalog.dto.ProductRequest;
import com.wrightlabs.marketplace.catalog.service.ProductService;
import com.wrightlabs.marketplace.admin.service.SellerDashboardService;
import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.User;
import com.wrightlabs.marketplace.storage.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final ProductService productService;
    private final SellerDashboardService dashboardService;
    private final FileStorageService fileStorageService;
    private final com.wrightlabs.marketplace.auth.service.UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        SellerDashboardService.SellerDashboardStats stats = dashboardService.getSellerStats(user.getId());

        model.addAttribute("stats", stats);
        model.addAttribute("topProducts", dashboardService.getTopSellingProducts(user.getId(), 5));
        model.addAttribute("salesData", dashboardService.getSalesByPeriod(user.getId(), 30));

        return "seller/dashboard";
    }

    @GetMapping("/products")
    public String listProducts(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        Page<Product> products = productService.getSellerProducts(user.getId(), PageRequest.of(page, 10));

        model.addAttribute("products", products);
        return "seller/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("isEdit", false);
        return "seller/product-form";
    }

    @PostMapping("/products")
    public String createProduct(@Valid @ModelAttribute ProductRequest request,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "seller/product-form";
        }

        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        Product product = productService.createProduct(request, user);

        redirectAttributes.addFlashAttribute("success", "Product created successfully");
        return "redirect:/seller/products/" + product.getId() + "/edit";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        Product product = productService.getProductById(id);

        // Verify ownership
        if (!product.getSeller().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied");
        }

        ProductRequest request = new ProductRequest();
        request.setTitle(product.getTitle());
        request.setSubtitle(product.getSubtitle());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice());

        model.addAttribute("productRequest", request);
        model.addAttribute("product", product);
        model.addAttribute("isEdit", true);

        return "seller/product-form";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id,
            @Valid @ModelAttribute ProductRequest request,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "seller/product-form";
        }

        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        productService.updateProduct(id, request, user);

        redirectAttributes.addFlashAttribute("success", "Product updated successfully");
        return "redirect:/seller/products/" + id + "/edit";
    }

    @PostMapping("/products/{id}/publish")
    public String publishProduct(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        productService.publishProduct(id, user);

        redirectAttributes.addFlashAttribute("success", "Product published successfully");
        return "redirect:/seller/products";
    }

    @PostMapping("/products/{id}/files")
    public String uploadFile(@PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean isPreview,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);
            fileStorageService.uploadFile(file, product, isPreview);

            redirectAttributes.addFlashAttribute("success", "File uploaded successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "File upload failed: " + e.getMessage());
        }

        return "redirect:/seller/products/" + id + "/edit";
    }
}
