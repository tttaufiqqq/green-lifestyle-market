package com.glm.payment;

import com.glm.checkout.FeeCalculator;
import com.glm.payment.dto.CallbackParams;
import com.glm.payment.entity.Payment;
import com.glm.payment.entity.WebhookEvent;
import com.glm.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Processes ToyyibPay callbacks (server-to-server) and browser return URLs.
 * Not @Transactional itself — each step commits individually so the webhook
 * insert is durable before external HTTP verification, per UC-07 R07-2.
 *
 * Flow: insertIfNew (TX1) → verifyPaid (HTTP) → settle/expire/review (TX2) → markProcessed (TX3).
 */
@Service
public class PaymentCallbackService {

    private static final Logger log = LoggerFactory.getLogger(PaymentCallbackService.class);

    private final PaymentRepository   paymentRepo;
    private final WebhookEventService eventService;
    private final ToyyibPayClient     toyyibPay;
    private final PaymentSettleService settleService;
    private final FeeCalculator        feeCalc;

    public PaymentCallbackService(PaymentRepository paymentRepo,
                                   WebhookEventService eventService,
                                   ToyyibPayClient toyyibPay,
                                   PaymentSettleService settleService,
                                   FeeCalculator feeCalc) {
        this.paymentRepo   = paymentRepo;
        this.eventService  = eventService;
        this.toyyibPay     = toyyibPay;
        this.settleService = settleService;
        this.feeCalc       = feeCalc;
    }

    /**
     * UC-07: Idempotent callback processing.
     * Duplicate key → no-op (FR-P1).
     * Amount mismatch or API error → REVIEW, admin notified (FR-P2).
     */
    public void processCallback(CallbackParams params) {
        Payment payment = paymentRepo.findByToyyibpayBillCode(params.billcode()).orElse(null);
        String key = params.billcode() + ":" + params.refno() + ":" + params.status();

        boolean isNew = eventService.insertIfNew(key, payment, toJson(params),
            WebhookEvent.Source.CALLBACK);
        if (!isNew) {
            log.info("[CB-DUP] key={}", key);
            return;
        }
        if (payment == null) {
            log.warn("[CB-UNKNOWN] billcode={}", params.billcode());
            return;
        }
        if (payment.getStatus() != Payment.Status.PENDING) {
            log.info("[CB-SKIP] already terminal paymentId={} status={}", payment.getId(), payment.getStatus());
            eventService.markProcessed(key);
            return;
        }

        try {
            long expectedSen = feeCalc.toSen(payment.getAmount());
            boolean paid = toyyibPay.verifyPaid(params.billcode(), expectedSen);
            if (paid) {
                settleService.settlePayment(payment.getId(), params.refno());
            } else {
                // status "3" = explicit failure; any other non-paid = treat as failure
                settleService.expirePayment(payment.getId());
            }
        } catch (Exception e) {
            // Verify API down or amount mismatch → flag REVIEW, sweeper will skip it
            log.error("[CB-VERIFY-ERR] billcode={} err={}", params.billcode(), e.getMessage());
            settleService.markReview(payment.getId());
        }

        eventService.markProcessed(key);
    }

    /**
     * Browser return URL handler — triggers the same verify path as the callback.
     * Returns the paymentNo so the controller can redirect to the SPA result page.
     * Safe to call even if the callback already settled the payment (idempotent settle).
     */
    public String processReturn(String billCode, String paymentNo) {
        Payment payment = billCode != null
            ? paymentRepo.findByToyyibpayBillCode(billCode).orElse(null) : null;
        if (payment == null)
            return paymentNo != null ? paymentNo : "unknown";

        if (payment.getStatus() == Payment.Status.PENDING) {
            String key = billCode + ":return:" + payment.getId();
            boolean isNew = eventService.insertIfNew(key, payment,
                "{\"source\":\"return\",\"billCode\":\"" + billCode + "\"}",
                WebhookEvent.Source.RETURN);
            if (isNew) {
                try {
                    long expectedSen = feeCalc.toSen(payment.getAmount());
                    if (toyyibPay.verifyPaid(billCode, expectedSen))
                        settleService.settlePayment(payment.getId(), "return-verify");
                } catch (Exception e) {
                    log.warn("[RETURN-VERIFY-ERR] billCode={}", billCode, e);
                }
                eventService.markProcessed(key);
            }
        }
        return payment.getPaymentNo();
    }

    private static String toJson(CallbackParams p) {
        return String.format(
            "{\"refno\":\"%s\",\"status\":\"%s\",\"billcode\":\"%s\",\"order_id\":\"%s\",\"amount\":\"%s\"}",
            p.refno(), p.status(), p.billcode(), p.orderId(), p.amount());
    }
}
