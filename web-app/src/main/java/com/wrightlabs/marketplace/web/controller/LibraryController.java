package com.wrightlabs.marketplace.web.controller;

import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.ProductFile;
import com.wrightlabs.marketplace.domain.entity.User;
import com.wrightlabs.marketplace.payment.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;
    private final com.wrightlabs.marketplace.auth.service.UserService userService;

    @GetMapping
    public String viewLibrary(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        List<Product> library = libraryService.getUserLibrary(user.getId());

        model.addAttribute("library", library);
        return "library/index";
    }

    @GetMapping("/download/{productId}/{fileId}")
    public String downloadFile(@PathVariable Long productId,
            @PathVariable Long fileId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        String downloadUrl = libraryService.generateDownloadUrl(user.getId(), productId, fileId);

        return "redirect:" + downloadUrl;
    }
}
