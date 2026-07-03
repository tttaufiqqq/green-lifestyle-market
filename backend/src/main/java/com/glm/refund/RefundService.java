package com.glm.refund;

import com.glm.common.audit.AuditLogRepository;
import com.glm.common.audit.AuditLog;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.notification.entity.Notification;
import com.glm.notification.repository.NotificationRepository;
import com.glm.order.entity.Order;
import com.glm.order.entity.Order.FulfilmentMethod;
import com.glm.order.entity.Order.Status;
import com.glm.order.lifecycle.StockRestorer;
import com.glm.order.repository.OrderRepository;
import com.glm.payout.repository.PayoutItemRepository;
import com.glm.refund.dto.RefundView;
import com.glm.refund.entity.Refund;
import com.glm.refund.repository.RefundRepository;
import com.glm.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final RefundRepository    refundRepo;
    private final OrderRepository     orderRepo;
    private final PayoutItemRepository payoutItemRepo;
    private final StockRestorer       stockRestorer;
    private final NotificationRepository notifRepo;
    private final AuditLogRepository  auditRepo;

    public RefundService(RefundRepository refundRepo, OrderRepository orderRepo,
                          PayoutItemRepository payoutItemRepo, StockRestorer stockRestorer,
                          NotificationRepository notifRepo, AuditLogRepository auditRepo) {
        this.refundRepo     = refundRepo;
        this.orderRepo      = orderRepo;
        this.payoutItemRepo = payoutItemRepo;
        this.stockRestorer  = stockRestorer;
        this.notifRepo      = notifRepo;
        this.auditRepo      = auditRepo;
    }

    @Transactional(readOnly = true)
    public List<RefundView> list(String statusFilter) {
        List<Refund> refunds = statusFilter != null
            ? refundRepo.findByStatus(Refund.Status.valueOf(statusFilter))
            : refundRepo.findAll();
        return refunds.stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public RefundView getById(Long id) {
        return toView(require(id));
    }

    /** Admin approves — refund moves to APPROVED; admin can then record the bank transfer. */
    @Transactional
    public void approve(Long refundId, User admin) {
        Refund refund = require(refundId);
        if (refund.getStatus() != Refund.Status.REQUESTED)
            throw new DomainException(ErrorCode.E_ORD_STATE, "Refund is not in REQUESTED state", 409);
        Instant now = Instant.now();
        refund.setStatus(Refund.Status.APPROVED);
        refund.setAdmin(admin);
        refund.setUpdatedAt(now);
        refundRepo.save(refund);
        audit(admin, "REFUND_APPROVE", "refund", refundId);
        notify(refund.getOrder().getBuyer(), "REFUND_APPROVED", "Refund approved",
            "Your refund for order " + refund.getOrder().getOrderNo() + " has been approved.");
        log.info("[REFUND] APPROVED refundId={} admin={}", refundId, admin.getId());
    }

    /** Admin rejects — refund REJECTED, order restored to prior status (before REFUND_REQUESTED). */
    @Transactional
    public void reject(Long refundId, User admin, String adminNote) {
        Refund refund = require(refundId);
        if (refund.getStatus() != Refund.Status.REQUESTED && refund.getStatus() != Refund.Status.APPROVED)
            throw new DomainException(ErrorCode.E_ORD_STATE, "Refund cannot be rejected in this state", 409);
        Instant now = Instant.now();
        refund.setStatus(Refund.Status.REJECTED);
        refund.setAdmin(admin);
        refund.setAdminNote(adminNote);
        refund.setUpdatedAt(now);
        refundRepo.save(refund);

        Order order = refund.getOrder();
        Status prior = priorStatus(order);
        order.setStatus(prior);
        order.setUpdatedAt(now);
        orderRepo.save(order);

        audit(admin, "REFUND_REJECT", "refund", refundId);
        notify(refund.getOrder().getBuyer(), "REFUND_REJECTED", "Refund rejected",
            "Your refund for order " + order.getOrderNo() + " was rejected. Reason: " + adminNote);
        log.info("[REFUND] REJECTED refundId={} priorStatus={}", refundId, prior);
    }

    /**
     * Admin processes the refund: bankRef mandatory → order REFUNDED, stock restored.
     * Blocked by E-REF-PAIDOUT if order already in a payout.
     * Written atomically with audit log in one TX.
     */
    @Transactional
    public void process(Long refundId, User admin, String bankRef, String adminNote) {
        Refund refund = require(refundId);
        if (refund.getStatus() != Refund.Status.APPROVED)
            throw new DomainException(ErrorCode.E_ORD_STATE, "Approve the refund before processing", 409);

        Order order = refund.getOrder();
        if (payoutItemRepo.existsByOrderId(order.getId()))
            throw new DomainException(ErrorCode.E_REF_PAIDOUT,
                "Order " + order.getOrderNo() + " has already been paid out", 409);

        Instant now = Instant.now();
        refund.setStatus(Refund.Status.PROCESSED);
        refund.setBankRef(bankRef);
        refund.setAdminNote(adminNote);
        refund.setProcessedAt(now);
        refund.setUpdatedAt(now);
        refundRepo.save(refund);

        order.setStatus(Status.REFUNDED);
        order.setUpdatedAt(now);
        orderRepo.save(order);

        stockRestorer.restore(order); // restore CONSUMED stock back to product

        audit(admin, "REFUND_PROCESS", "refund", refundId);
        notify(order.getBuyer(), "REFUNDED", "Refund processed",
            "Your refund of RM " + refund.getAmount() + " for order "
                + order.getOrderNo() + " has been processed. Bank ref: " + bankRef);
        notify(order.getSeller(), "REFUNDED", "Order refunded",
            "Order " + order.getOrderNo() + " has been refunded to the buyer.");
        log.info("[REFUND] PROCESSED refundId={} orderId={} bankRef={}", refundId, order.getId(), bankRef);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Refund require(Long id) {
        return refundRepo.findById(id).orElseThrow(() ->
            new DomainException(ErrorCode.E_NOTFOUND, "Refund not found: " + id, 404));
    }

    private Status priorStatus(Order order) {
        if (order.getShippedAt() != null)
            return order.getFulfilmentMethod() == FulfilmentMethod.MEETUP
                ? Status.READY_FOR_MEETUP : Status.SHIPPED;
        return Status.CONFIRMED;
    }

    private void audit(User admin, String action, String entityType, Long entityId) {
        AuditLog log = new AuditLog();
        log.setUser(admin);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setCreatedAt(Instant.now());
        auditRepo.save(log);
    }

    private void notify(User user, String type, String title, String body) {
        Notification n = new Notification();
        n.setUser(user); n.setType(type); n.setTitle(title); n.setBody(body);
        n.setCreatedAt(Instant.now());
        notifRepo.save(n);
    }

    private RefundView toView(Refund r) {
        return new RefundView(r.getId(), r.getOrder().getOrderNo(),
            r.getOrder().getBuyer().getName(), r.getOrder().getSeller().getName(),
            r.getAmount(), r.getStatus().name(), r.getReason(),
            r.getAdminNote(), r.getBankRef(), r.getCreatedAt(), r.getProcessedAt());
    }
}
