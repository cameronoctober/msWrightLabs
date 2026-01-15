package com.wrightlabs.marketplace.domain.repository;

import com.wrightlabs.marketplace.domain.entity.Download;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DownloadRepository extends JpaRepository<Download, Long> {

    Page<Download> findByUserId(Long userId, Pageable pageable);

    Page<Download> findByProductId(Long productId, Pageable pageable);

    Long countByProductId(Long productId);
}
