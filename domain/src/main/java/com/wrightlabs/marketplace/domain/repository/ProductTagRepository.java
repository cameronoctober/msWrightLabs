package com.wrightlabs.marketplace.domain.repository;

import com.wrightlabs.marketplace.domain.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {

    List<ProductTag> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
