package com.wrightlabs.marketplace.payment.service;

import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.User;
import com.wrightlabs.marketplace.domain.entity.Wishlist;
import com.wrightlabs.marketplace.domain.repository.ProductRepository;
import com.wrightlabs.marketplace.domain.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void addToWishlist(Long productId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Check if already in wishlist
        if (wishlistRepository.findByUserIdAndProductId(user.getId(), productId).isPresent()) {
            throw new IllegalArgumentException("Product already in wishlist");
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        wishlistRepository.save(wishlist);
        log.info("Added product {} to wishlist for user {}", productId, user.getEmail());
    }

    @Transactional
    public void removeFromWishlist(Long productId, User user) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not in wishlist"));

        wishlistRepository.delete(wishlist);
        log.info("Removed product {} from wishlist for user {}", productId, user.getEmail());
    }

    public List<Product> getUserWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(Wishlist::getProduct)
                .collect(Collectors.toList());
    }

    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }
}
