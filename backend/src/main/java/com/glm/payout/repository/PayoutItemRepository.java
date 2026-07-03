package com.glm.payout.repository;

import com.glm.payout.entity.PayoutItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PayoutItemRepository extends JpaRepository<PayoutItem, Long> {
    List<PayoutItem> findByPayoutId(Long payoutId);
    Optional<PayoutItem> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}
