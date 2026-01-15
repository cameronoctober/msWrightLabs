package com.wrightlabs.marketplace.admin.service;

import com.wrightlabs.marketplace.domain.entity.Order;
import com.wrightlabs.marketplace.domain.entity.OrderItem;
import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.repository.OrderRepository;
import com.wrightlabs.marketplace.domain.repository.ProductRepository;
import lombok.Data;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SellerDashboardService {

        private final OrderRepository orderRepository;
        private final ProductRepository productRepository;

        public SellerDashboardStats getSellerStats(Long sellerId) {
                // Get all seller's products
                List<Product> products = productRepository.findBySellerId(sellerId, Pageable.unpaged()).getContent();

                // Get all orders containing seller's products
                List<Order> orders = orderRepository.findAll().stream()
                                .filter(order -> order.getStatus() == Order.OrderStatus.PAID)
                                .filter(order -> order.getItems().stream()
                                                .anyMatch(item -> item.getSeller().getId().equals(sellerId)))
                                .collect(Collectors.toList());

                // Calculate stats
                int totalSales = orders.size();

                BigDecimal totalRevenue = orders.stream()
                                .flatMap(order -> order.getItems().stream())
                                .filter(item -> item.getSeller().getId().equals(sellerId))
                                .map(OrderItem::getSellerAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                int totalProducts = products.size();

                int publishedProducts = (int) products.stream()
                                .filter(p -> p.getStatus() == Product.ProductStatus.PUBLISHED)
                                .count();

                SellerDashboardStats stats = new SellerDashboardStats();
                stats.setTotalSales(totalSales);
                stats.setTotalRevenue(totalRevenue);
                stats.setTotalProducts(totalProducts);
                stats.setPublishedProducts(publishedProducts);

                return stats;
        }

        public List<Product> getTopSellingProducts(Long sellerId, int limit) {
                Page<Product> products = productRepository.findBySellerId(sellerId,
                                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "purchases")));

                return products.getContent();
        }

        public Map<String, Integer> getSalesByPeriod(Long sellerId, int days) {
                LocalDateTime startDate = LocalDateTime.now().minusDays(days);

                List<Order> orders = orderRepository.findAll().stream()
                                .filter(order -> order.getStatus() == Order.OrderStatus.PAID)
                                .filter(order -> order.getCreatedAt().isAfter(startDate))
                                .filter(order -> order.getItems().stream()
                                                .anyMatch(item -> item.getSeller().getId().equals(sellerId)))
                                .collect(Collectors.toList());

                Map<String, Integer> salesByDate = new HashMap<>();
                for (Order order : orders) {
                        String date = order.getCreatedAt().toLocalDate().toString();
                        salesByDate.put(date, salesByDate.getOrDefault(date, 0) + 1);
                }

                return salesByDate;
        }

        @Data
        public static class SellerDashboardStats {
                private int totalSales;
                private BigDecimal totalRevenue;
                private int totalProducts;
                private int publishedProducts;
        }
}
