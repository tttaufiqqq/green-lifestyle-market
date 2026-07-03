package com.glm.payment.repository;

import com.glm.payment.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    Optional<WebhookEvent> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);
}
