package com.glm.payment;

import com.glm.payment.entity.Payment;
import com.glm.payment.entity.WebhookEvent;
import com.glm.payment.repository.WebhookEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Idempotency ledger for ToyyibPay events.
 * Each method runs in its own committed transaction so the insert is
 * durable before the caller proceeds with (slow) external HTTP verification.
 */
@Service
public class WebhookEventService {

    private final WebhookEventRepository webhookEventRepo;

    public WebhookEventService(WebhookEventRepository webhookEventRepo) {
        this.webhookEventRepo = webhookEventRepo;
    }

    /**
     * Inserts the event row if the idempotency key is new.
     * Returns true if this is the first occurrence (should be processed);
     * returns false if duplicate (processing already done or in progress).
     * The UNIQUE constraint on idempotency_key is the DB-level guard.
     */
    @Transactional
    public boolean insertIfNew(String key, Payment payment, String payload,
                                WebhookEvent.Source source) {
        if (webhookEventRepo.existsByIdempotencyKey(key)) return false;
        WebhookEvent evt = new WebhookEvent();
        evt.setIdempotencyKey(key);
        evt.setPayment(payment);
        evt.setSource(source);
        evt.setRawPayload(payload);
        evt.setProcessed(false);
        evt.setCreatedAt(Instant.now());
        webhookEventRepo.save(evt);
        return true;
    }

    @Transactional
    public void markProcessed(String key) {
        webhookEventRepo.findByIdempotencyKey(key).ifPresent(evt -> {
            evt.setProcessed(true);
            evt.setProcessedAt(Instant.now());
            webhookEventRepo.save(evt);
        });
    }
}
