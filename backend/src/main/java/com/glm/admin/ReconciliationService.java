package com.glm.admin;

import com.glm.admin.dto.ReconciliationRow;
import com.glm.notification.entity.Notification;
import com.glm.notification.repository.NotificationRepository;
import com.glm.payment.PaymentCallbackService;
import com.glm.payment.ToyyibPayClient;
import com.glm.payment.entity.Payment;
import com.glm.payment.repository.PaymentRepository;
import com.glm.user.entity.User;
import com.glm.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Reconciliation: compares local payment records against ToyyibPay gateway for a given date.
 * Missed callbacks are healed by running the verify path.
 * Discrepancies (local SUCCESS but gateway says not paid) produce ADMIN_ALERT.
 */
@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);
    private static final ZoneId MYT = ZoneId.of("Asia/Kuala_Lumpur");

    private final PaymentRepository      paymentRepo;
    private final ToyyibPayClient        toyyibPay;
    private final PaymentCallbackService callbackService;
    private final NotificationRepository notifRepo;
    private final UserRepository         userRepo;

    public ReconciliationService(PaymentRepository paymentRepo, ToyyibPayClient toyyibPay,
                                  PaymentCallbackService callbackService,
                                  NotificationRepository notifRepo, UserRepository userRepo) {
        this.paymentRepo     = paymentRepo;
        this.toyyibPay       = toyyibPay;
        this.callbackService = callbackService;
        this.notifRepo       = notifRepo;
        this.userRepo        = userRepo;
    }

    /** On-demand: run reconciliation for a specific date (MYT midnight → midnight). */
    public List<ReconciliationRow> runForDate(LocalDate date) {
        Instant from = date.atStartOfDay(MYT).toInstant();
        Instant to   = date.plusDays(1).atStartOfDay(MYT).toInstant();
        List<Payment> payments = paymentRepo.findByCreatedAtBetweenOrderByCreatedAtAsc(from, to);
        List<ReconciliationRow> rows = new ArrayList<>();
        int discrepancies = 0;

        for (Payment p : payments) {
            if (p.getToyyibpayBillCode() == null) continue;
            long expectedSen = new BigDecimal(100).multiply(p.getAmount()).longValue();
            boolean gatewayPaid;
            try {
                gatewayPaid = toyyibPay.verifyPaid(p.getToyyibpayBillCode(), expectedSen);
            } catch (Exception e) {
                log.warn("[RECON] verifyPaid failed paymentNo={}", p.getPaymentNo(), e);
                rows.add(row(p, false, "GATEWAY_ERROR: " + e.getMessage()));
                discrepancies++;
                continue;
            }

            String issue = null;
            if (!gatewayPaid && p.getStatus() == Payment.Status.SUCCESS) {
                issue = "CRITICAL: local=SUCCESS but gateway not paid";
                discrepancies++;
            } else if (gatewayPaid && p.getStatus() == Payment.Status.PENDING) {
                issue = "MISSED_CALLBACK: healing now";
                discrepancies++;
                try {
                    callbackService.processReturn(p.getToyyibpayBillCode(), p.getPaymentNo());
                } catch (Exception e) {
                    log.error("[RECON] heal failed paymentNo={}", p.getPaymentNo(), e);
                }
            } else if (gatewayPaid && p.getStatus() == Payment.Status.REVIEW) {
                issue = "REVIEW: manual check needed";
                discrepancies++;
            }
            rows.add(row(p, gatewayPaid, issue));
        }

        if (discrepancies > 0) alertAdmins(date, discrepancies);
        log.info("[RECON] date={} payments={} discrepancies={}", date, payments.size(), discrepancies);
        return rows;
    }

    /** Scheduled entry-point (called by ReconciliationJob with yesterday's date). */
    @Transactional
    public List<ReconciliationRow> runScheduled() {
        return runForDate(LocalDate.now(MYT).minusDays(1));
    }

    private ReconciliationRow row(Payment p, boolean gatewayPaid, String issue) {
        return new ReconciliationRow(p.getPaymentNo(), p.getToyyibpayBillCode(),
            p.getStatus().name(), gatewayPaid, p.getAmount(), issue);
    }

    private void alertAdmins(LocalDate date, int count) {
        userRepo.findAll().stream()
            .filter(u -> u.getRole() == User.Role.ADMIN)
            .forEach(admin -> {
                Notification n = new Notification();
                n.setUser(admin);
                n.setType("ADMIN_ALERT");
                n.setTitle("Reconciliation alert");
                n.setBody("Reconciliation for " + date + " found " + count + " discrepancies. Please review.");
                n.setCreatedAt(Instant.now());
                notifRepo.save(n);
            });
    }
}
