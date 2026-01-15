package com.wrightlabs.marketplace.domain.repository;

import com.wrightlabs.marketplace.domain.entity.ProductFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFileRepository extends JpaRepository<ProductFile, Long> {

    List<ProductFile> findByProductIdOrderBySortOrderAsc(Long productId);

    List<ProductFile> findByProductIdAndIsPreviewTrue(Long productId);

    List<ProductFile> findByProductId(Long productId);
}
