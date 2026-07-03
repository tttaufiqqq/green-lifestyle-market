package com.glm.payment;

import com.glm.payment.entity.Payment;
import com.glm.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * UC-08: Every 60 s, find PENDING payments past expires_at and expire them.
 * REVIEW-status payments are naturally excluded because they are no longer PENDING.
 * Row-level locking inside PaymentSettleService.expirePayment handles races with
 * an in-flight callback arriving simultaneously (UC-08 R08-E1).
 */
@Component
public class PaymentSweeper {

    private static final Logger log = LoggerFactory.getLogger(PaymentSweeper.class);

    private final PaymentRepository  paymentRepo;
    private final PaymentSettleService settleService;

    public PaymentSweeper(PaymentRepository paymentRepo, PaymentSettleService settleService) {
        this.paymentRepo   = paymentRepo;
        this.settleService = settleService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void sweep() {
        List<Payment> expired = paymentRepo.findByStatusAndExpiresAtBefore(
            Payment.Status.PENDING, Instant.now());
        if (expired.isEmpty()) return;

        log.info("[SWEEPER] expiring {} payment(s)", expired.size());
        for (Payment p : expired) {
            try {
                settleService.expirePayment(p.getId());
            } catch (Exception e) {
                // Log and continue — one bad record must not block the rest
                log.error("[SWEEPER] error expiring paymentId={}: {}", p.getId(), e.getMessage());
            }
        }
    }
}
