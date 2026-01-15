package com.wrightlabs.marketplace.payment.service;

import com.wrightlabs.marketplace.domain.entity.Cart;
import com.wrightlabs.marketplace.domain.entity.CartItem;
import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.User;
import com.wrightlabs.marketplace.domain.repository.CartItemRepository;
import com.wrightlabs.marketplace.domain.repository.CartRepository;
import com.wrightlabs.marketplace.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public Cart getOrCreateCart(User user, String sessionId) {
        Cart cart;

        if (user != null) {
            cart = cartRepository.findByUserId(user.getId())
                    .orElseGet(() -> createCart(user, null));
        } else {
            cart = cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> createCart(null, sessionId));
        }

        return cart;
    }

    @Transactional
    public Cart addToCart(Long productId, User user, String sessionId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Cart cart = getOrCreateCart(user, sessionId);

        // Check if product already in cart
        boolean exists = cart.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (exists) {
            throw new IllegalArgumentException("Product already in cart");
        }

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(1) // Digital products, quantity is always 1
                .build();

        cartItemRepository.save(cartItem);
        log.info("Added product {} to cart", productId);

        return cartRepository.findById(cart.getId()).get();
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        cartItemRepository.delete(cartItem);
        log.info("Removed cart item {}", cartItemId);
    }

    public BigDecimal calculateCartTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getProduct().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<CartItem> getCartItems(Cart cart) {
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Transactional
    public void clearCart(Cart cart) {
        cartItemRepository.deleteAll(cart.getItems());
        log.info("Cleared cart {}", cart.getId());
    }

    private Cart createCart(User user, String sessionId) {
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }

        Cart cart = Cart.builder()
                .user(user)
                .sessionId(sessionId)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        return cartRepository.save(cart);
    }
}
