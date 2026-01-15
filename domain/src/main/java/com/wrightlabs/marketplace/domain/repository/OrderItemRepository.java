package com.wrightlabs.marketplace.domain.repository;

import com.wrightlabs.marketplace.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findBySellerId(Long sellerId);

    @Query("SELECT SUM(oi.sellerAmount) FROM OrderItem oi " +
            "WHERE oi.seller.id = :sellerId " +
            "AND oi.order.status = 'PAID' " +
            "AND oi.order.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateSellerRevenue(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
