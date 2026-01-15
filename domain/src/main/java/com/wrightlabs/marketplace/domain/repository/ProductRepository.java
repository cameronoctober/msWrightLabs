package com.wrightlabs.marketplace.domain.repository;

import com.wrightlabs.marketplace.domain.entity.Product;
import com.wrightlabs.marketplace.domain.entity.Product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findBySellerIdAndStatus(Long sellerId, ProductStatus status, Pageable pageable);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' ORDER BY p.purchases DESC")
    List<Product> findBestsellers(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'PUBLISHED' ORDER BY p.createdAt DESC")
    List<Product> findNewest(Pageable pageable);

    @Query("SELECT p FROM Product p JOIN Review r ON r.product = p " +
            "WHERE p.status = 'PUBLISHED' AND r.status = 'APPROVED' " +
            "GROUP BY p.id ORDER BY AVG(r.rating) DESC")
    List<Product> findTopRated(Pageable pageable);
}
