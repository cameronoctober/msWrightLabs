package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.catalog.dto.ProductSearchCriteria;
import com.wrightlabs.marketplace.catalog.service.ProductService;
import com.wrightlabs.marketplace.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        // Fetch featured products for homepage
        List<Product> bestsellers = productService.getBestsellers(6);
        List<Product> newest = productService.getNewest(6);
        List<Product> topRated = productService.getTopRated(6);

        model.addAttribute("bestsellers", bestsellers);
        model.addAttribute("newest", newest);
        model.addAttribute("topRated", topRated);

        return "index";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setKeyword(keyword);

        Page<Product> products = productService.searchProducts(criteria, pageable);

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);

        return "search";
    }
}
