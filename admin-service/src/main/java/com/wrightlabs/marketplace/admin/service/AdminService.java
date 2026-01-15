package com.wrightlabs.marketplace.admin.service;

import com.wrightlabs.marketplace.domain.entity.*;
import com.wrightlabs.marketplace.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    // User Management
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public void lockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEnabled(false);
        userRepository.save(user);
        log.info("Locked user: {}", user.getEmail());
    }

    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
        log.info("Unlocked user: {}", user.getEmail());
    }

    // Product Moderation
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> getPendingProducts(Pageable pageable) {
        return productRepository.findByStatus(Product.ProductStatus.DRAFT, pageable);
    }

    @Transactional
    public void approveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.setStatus(Product.ProductStatus.PUBLISHED);
        productRepository.save(product);
        log.info("Approved product: {}", product.getTitle());
    }

    @Transactional
    public void archiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.setStatus(Product.ProductStatus.ARCHIVED);
        productRepository.save(product);
        log.info("Archived product: {}", product.getTitle());
    }

    // Review Moderation
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    public Page<Review> getPendingReviews(Pageable pageable) {
        return reviewRepository.findByStatus(Review.ReviewStatus.PENDING, pageable);
    }

    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setStatus(Review.ReviewStatus.APPROVED);
        reviewRepository.save(review);
        log.info("Approved review: {}", reviewId);
    }

    @Transactional
    public void rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setStatus(Review.ReviewStatus.REJECTED);
        reviewRepository.save(review);
        log.info("Rejected review: {}", reviewId);
    }

    // Analytics
    public BigDecimal getTotalRevenue() {
        List<Order> paidOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.PAID)
                .toList();

        return paidOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public long getTotalProducts() {
        return productRepository.count();
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findAll(PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }
}
