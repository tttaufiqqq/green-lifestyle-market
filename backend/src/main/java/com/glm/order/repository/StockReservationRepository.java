package com.glm.order.repository;

import com.glm.order.entity.StockReservation;
import com.glm.order.entity.StockReservation.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    List<StockReservation> findByProductIdAndStatus(Long productId, Status status);
    List<StockReservation> findByStatusAndExpiresAtBefore(Status status, Instant now);
    List<StockReservation> findByOrderId(Long orderId);
}
