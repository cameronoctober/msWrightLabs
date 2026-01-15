package com.wrightlabs.marketplace.payment.service;

import com.wrightlabs.marketplace.domain.entity.Download;
import com.wrightlabs.marketplace.domain.entity.Order;
import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.ProductFile;
import com.wrightlabs.marketplace.domain.repository.DownloadRepository;
import com.wrightlabs.marketplace.domain.repository.OrderRepository;
import com.wrightlabs.marketplace.domain.repository.ProductFileRepository;
import com.wrightlabs.marketplace.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LibraryService {

    private final OrderRepository orderRepository;
    private final ProductFileRepository productFileRepository;
    private final DownloadRepository downloadRepository;
    private final FileStorageService fileStorageService;

    public List<Product> getUserLibrary(Long userId) {
        // Get all paid orders for user
        List<Order> paidOrders = orderRepository.findByBuyerId(userId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.PAID)
                .collect(Collectors.toList());

        // Extract unique products from all order items
        return paidOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(item -> item.getProduct())
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean userOwnsProduct(Long userId, Long productId) {
        List<Product> library = getUserLibrary(userId);
        return library.stream().anyMatch(p -> p.getId().equals(productId));
    }

    public String generateDownloadUrl(Long userId, Long productId, Long fileId) {
        // Verify user owns the product
        if (!userOwnsProduct(userId, productId)) {
            throw new IllegalArgumentException("You do not own this product");
        }

        ProductFile file = productFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        if (!file.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("File does not belong to this product");
        }

        // Generate presigned URL
        String downloadUrl = fileStorageService.generateDownloadUrl(file.getFileKey());

        // Record download
        recordDownload(userId, productId, file.getFileKey());

        log.info("Generated download URL for user {} product {} file {}", userId, productId, fileId);
        return downloadUrl;
    }

    @Transactional
    public void recordDownload(Long userId, Long productId, String fileKey) {
        // Find the order for this user and product
        Order order = orderRepository.findByBuyerId(userId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PAID)
                .filter(o -> o.getItems().stream()
                        .anyMatch(item -> item.getProduct().getId().equals(productId)))
                .findFirst()
                .orElse(null);

        if (order != null) {
            Product product = order.getItems().stream()
                    .map(com.wrightlabs.marketplace.domain.entity.OrderItem::getProduct)
                    .filter(p -> p.getId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Product not found in order"));

            Download download = Download.builder()
                    .order(order)
                    .product(product)
                    .user(order.getBuyer())
                    .fileKey(fileKey)
                    .build();

            downloadRepository.save(download);
        }
    }

    public List<ProductFile> getProductFiles(Long productId) {
        return productFileRepository.findByProductId(productId);
    }
}
