package com.glm.refund.repository;

import com.glm.refund.entity.Refund;
import com.glm.refund.entity.Refund.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByOrderId(Long orderId);
    List<Refund> findByStatus(Status status);
}
