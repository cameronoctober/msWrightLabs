package com.wrightlabs.marketplace.catalog.service;

import com.wrightlabs.marketplace.catalog.dto.ReviewRequest;
import com.wrightlabs.marketplace.domain.entity.Order;
import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.Review;
import com.wrightlabs.marketplace.domain.entity.User;
import com.wrightlabs.marketplace.domain.repository.OrderRepository;
import com.wrightlabs.marketplace.domain.repository.ProductRepository;
import com.wrightlabs.marketplace.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Review submitReview(ReviewRequest request, User user) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Verify user has purchased the product
        boolean hasPurchased = orderRepository.findAll().stream()
                .filter(order -> order.getBuyer() != null && order.getBuyer().getId().equals(user.getId()))
                .filter(order -> order.getStatus() == Order.OrderStatus.PAID)
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct() != null && item.getProduct().getId().equals(product.getId()));

        if (!hasPurchased) {
            throw new IllegalArgumentException("You must purchase this product before leaving a review");
        }

        // Check if user has already reviewed this product
        if (reviewRepository.findByProductIdAndUserId(product.getId(), user.getId()).isPresent()) {
            throw new IllegalArgumentException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .status(Review.ReviewStatus.APPROVED) // Auto-approve for now, can change to PENDING
                .build();

        review = reviewRepository.save(review);
        log.info("User {} submitted review for product {}", user.getEmail(), product.getTitle());

        return review;
    }

    public Page<Review> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndStatus(productId, Review.ReviewStatus.APPROVED, pageable);
    }

    public Double getAverageRating(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndStatus(
                productId, Review.ReviewStatus.APPROVED, Pageable.unpaged()).getContent();

        if (reviews.isEmpty()) {
            return null;
        }

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public long getReviewCount(Long productId) {
        return reviewRepository.countByProductIdAndStatus(productId, Review.ReviewStatus.APPROVED);
    }

    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setStatus(Review.ReviewStatus.APPROVED);
        reviewRepository.save(review);
        log.info("Approved review {}", reviewId);
    }

    @Transactional
    public void rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setStatus(Review.ReviewStatus.REJECTED);
        reviewRepository.save(review);
        log.info("Rejected review {}", reviewId);
    }

    public Page<Review> getPendingReviews(Pageable pageable) {
        return reviewRepository.findByStatus(Review.ReviewStatus.PENDING, pageable);
    }
}
