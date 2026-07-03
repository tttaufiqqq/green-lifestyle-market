package com.glm.payout.repository;

import com.glm.payout.entity.Payout;
import com.glm.payout.entity.Payout.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    Optional<Payout> findByPayoutNo(String payoutNo);
    List<Payout> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    List<Payout> findByStatus(Status status);
}
