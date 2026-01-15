package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.catalog.dto.ProductSearchCriteria;
import com.wrightlabs.marketplace.catalog.service.ProductService;
import com.wrightlabs.marketplace.catalog.service.ReviewService;
import com.wrightlabs.marketplace.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ReviewService reviewService;

    @GetMapping
    public String listProducts(@ModelAttribute ProductSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        Page<Product> products = productService.searchProducts(criteria, PageRequest.of(page, size));

        model.addAttribute("products", products);
        model.addAttribute("criteria", criteria);
        return "products/list";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        productService.incrementViewCount(id);

        Double averageRating = reviewService.getAverageRating(id);
        long reviewCount = reviewService.getReviewCount(id);
        Page reviews = reviewService.getProductReviews(id, PageRequest.of(0, 10));

        model.addAttribute("product", product);
        model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("reviews", reviews);

        return "products/detail";
    }
}
