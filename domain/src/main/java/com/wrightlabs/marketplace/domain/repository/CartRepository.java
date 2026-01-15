package com.wrightlabs.marketplace.domain.repository;

import com.wrightlabs.marketplace.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findBySessionId(String sessionId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
