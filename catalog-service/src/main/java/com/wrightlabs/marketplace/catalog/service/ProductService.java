package com.wrightlabs.marketplace.catalog.service;

import com.wrightlabs.marketplace.catalog.dto.ProductRequest;
import com.wrightlabs.marketplace.catalog.dto.ProductSearchCriteria;
import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.ProductTag;
import com.wrightlabs.marketplace.domain.entity.User;
import com.wrightlabs.marketplace.domain.repository.ProductRepository;
import com.wrightlabs.marketplace.domain.repository.ProductTagRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductTagRepository productTagRepository;

    public List<Product> getBestsellers(int limit) {
        return productRepository.findBestsellers(PageRequest.of(0, limit));
    }

    public List<Product> getNewest(int limit) {
        return productRepository.findNewest(PageRequest.of(0, limit));
    }

    public List<Product> getTopRated(int limit) {
        return productRepository.findTopRated(PageRequest.of(0, limit));
    }

    public Page<Product> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        Specification<Product> spec = buildSearchSpecification(criteria);
        Pageable sortedPageable = applySorting(criteria.getSortBy(), pageable);
        return productRepository.findAll(spec, sortedPageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Transactional
    public Product createProduct(ProductRequest request, User seller) {
        Map<String, Object> metadata = new HashMap<>();
        if (request.getGrade() != null)
            metadata.put("grade", request.getGrade());
        if (request.getSubject() != null)
            metadata.put("subject", request.getSubject());
        if (request.getResourceType() != null)
            metadata.put("resourceType", request.getResourceType());
        if (request.getPageCount() != null)
            metadata.put("pageCount", request.getPageCount());
        if (request.getFileFormat() != null)
            metadata.put("fileFormat", request.getFileFormat());

        Product product = Product.builder()
                .seller(seller)
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .status(Product.ProductStatus.DRAFT)
                .metadata(metadata)
                .views(0)
                .purchases(0)
                .build();

        product = productRepository.save(product);
        log.info("Created product: {} by seller: {}", product.getTitle(), seller.getEmail());

        // Add tags if provided
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            addTags(product, request.getTags());
        }

        return product;
    }

    @Transactional
    public Product updateProduct(Long productId, ProductRequest request, User seller) {
        Product product = getProductById(productId);

        // Verify ownership
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new IllegalArgumentException("You do not have permission to edit this product");
        }

        product.setTitle(request.getTitle());
        product.setSubtitle(request.getSubtitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCurrency(request.getCurrency());

        // Update metadata
        Map<String, Object> metadata = new HashMap<>();
        if (request.getGrade() != null)
            metadata.put("grade", request.getGrade());
        if (request.getSubject() != null)
            metadata.put("subject", request.getSubject());
        if (request.getResourceType() != null)
            metadata.put("resourceType", request.getResourceType());
        if (request.getPageCount() != null)
            metadata.put("pageCount", request.getPageCount());
        if (request.getFileFormat() != null)
            metadata.put("fileFormat", request.getFileFormat());
        product.setMetadata(metadata);

        // Update tags
        if (request.getTags() != null) {
            productTagRepository.deleteAll(product.getTags());
            product.getTags().clear();
            addTags(product, request.getTags());
        }

        product = productRepository.save(product);
        log.info("Updated product: {}", product.getTitle());

        return product;
    }

    @Transactional
    public void publishProduct(Long productId, User seller) {
        Product product = getProductById(productId);

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new IllegalArgumentException("You do not have permission to publish this product");
        }

        product.setStatus(Product.ProductStatus.PUBLISHED);
        productRepository.save(product);
        log.info("Published product: {}", product.getTitle());
    }

    @Transactional
    public void unpublishProduct(Long productId, User seller) {
        Product product = getProductById(productId);

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new IllegalArgumentException("You do not have permission to unpublish this product");
        }

        product.setStatus(Product.ProductStatus.DRAFT);
        productRepository.save(product);
        log.info("Unpublished product: {}", product.getTitle());
    }

    @Transactional
    public void incrementViewCount(Long productId) {
        Product product = getProductById(productId);
        product.setViews(product.getViews() + 1);
        productRepository.save(product);
    }

    @Transactional
    public void incrementPurchaseCount(Long productId) {
        Product product = getProductById(productId);
        product.setPurchases(product.getPurchases() + 1);
        productRepository.save(product);
    }

    public Page<Product> getSellerProducts(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable);
    }

    @Transactional
    public void deleteProduct(Long productId, User seller) {
        Product product = getProductById(productId);

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new IllegalArgumentException("You do not have permission to delete this product");
        }

        productRepository.delete(product);
        log.info("Deleted product: {}", product.getTitle());
    }

    private void addTags(Product product, Set<String> tags) {
        for (String tagName : tags) {
            ProductTag tag = ProductTag.builder()
                    .product(product)
                    .tag(tagName.trim())
                    .build();
            product.getTags().add(tag);
            productTagRepository.save(tag);
        }
    }

    private Specification<Product> buildSearchSpecification(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only show published products in search
            predicates.add(cb.equal(root.get("status"), Product.ProductStatus.PUBLISHED));

            // Keyword search (title, subtitle, description)
            if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
                String likePattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), likePattern);
                Predicate subtitleMatch = cb.like(cb.lower(root.get("subtitle")), likePattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), likePattern);
                predicates.add(cb.or(titleMatch, subtitleMatch, descMatch));
            }

            // Price range
            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }

            // Metadata filtering (grade, subject, resourceType)
            if (criteria.getGrade() != null) {
                predicates.add(cb.equal(
                        cb.function("jsonb_extract_path_text", String.class,
                                root.get("metadata"), cb.literal("grade")),
                        criteria.getGrade()));
            }

            if (criteria.getSubject() != null) {
                predicates.add(cb.equal(
                        cb.function("jsonb_extract_path_text", String.class,
                                root.get("metadata"), cb.literal("subject")),
                        criteria.getSubject()));
            }

            if (criteria.getResourceType() != null) {
                predicates.add(cb.equal(
                        cb.function("jsonb_extract_path_text", String.class,
                                root.get("metadata"), cb.literal("resourceType")),
                        criteria.getResourceType()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable applySorting(String sortBy, Pageable pageable) {
        if (sortBy == null || sortBy.isBlank()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Sort sort = switch (sortBy.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "popularity" -> Sort.by(Sort.Direction.DESC, "purchases");
            case "rating" -> Sort.by(Sort.Direction.DESC, "id"); // TODO: Implement actual rating sort
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // newest
        };

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
