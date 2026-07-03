package com.glm.payment.repository;

import com.glm.payment.entity.Payment;
import com.glm.payment.entity.Payment.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentNo(String paymentNo);
    Optional<Payment> findByToyyibpayBillCode(String billCode);
    List<Payment> findByStatusAndExpiresAtBefore(Status status, Instant now);
    long countByStatus(Status status);
    List<Payment> findByCreatedAtBetweenOrderByCreatedAtAsc(Instant from, Instant to);
    List<Payment> findByStatus(Status status);
}
