package com.wrightlabs.marketplace.domain.repository;

import com.wrightlabs.marketplace.domain.entity.Payout;
import com.wrightlabs.marketplace.domain.entity.Payout.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {

    Page<Payout> findBySellerId(Long sellerId, Pageable pageable);

    Page<Payout> findByStatus(PayoutStatus status, Pageable pageable);
}
