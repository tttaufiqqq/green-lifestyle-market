package com.glm.order;

import com.glm.order.dto.MeetupRequest;
import com.glm.order.dto.ShipRequest;
import com.glm.order.entity.Order;
import com.glm.order.entity.Order.Status;
import com.glm.order.lifecycle.OrderNotifier;
import com.glm.order.lifecycle.RefundCreator;
import com.glm.order.lifecycle.StockRestorer;
import com.glm.order.lifecycle.TransitionGuard;
import com.glm.order.repository.OrderRepository;
import com.glm.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Enforces the order state machine from docs/domain-rules.md.
 * All transitions are atomic (@Transactional).
 * Guard → effects → side-effects (stock, refund, notifications) — in that order.
 */
@Service
public class OrderTransitionService {

    private static final Logger log = LoggerFactory.getLogger(OrderTransitionService.class);

    private final OrderRepository orderRepo;
    private final TransitionGuard guard;
    private final StockRestorer stockRestorer;
    private final RefundCreator refundCreator;
    private final OrderNotifier notifier;

    public OrderTransitionService(OrderRepository orderRepo, TransitionGuard guard,
                                   StockRestorer stockRestorer, RefundCreator refundCreator,
                                   OrderNotifier notifier) {
        this.orderRepo     = orderRepo;
        this.guard         = guard;
        this.stockRestorer = stockRestorer;
        this.refundCreator = refundCreator;
        this.notifier      = notifier;
    }

    @Transactional
    public void buyerCancel(Order order, User actor, String reason) {
        Status next = guard.requireLegal(order, "BUYER_CANCEL");
        boolean wasPaid = order.getStatus() == Status.PAID;
        if (next == Status.CANCELLED) order.setCancelledAt(Instant.now());
        applyStatus(order, next, reason);
        stockRestorer.restore(order);
        if (wasPaid) {
            refundCreator.create(order, actor, reason);
        }
        notifier.onCancelled(order, reason);
        log.info("[ORDER] BUYER_CANCEL orderNo={} actor={} wasPaid={}", order.getOrderNo(), actor.getId(), wasPaid);
    }

    @Transactional
    public void confirm(Order order) {
        guard.requireLegal(order, "CONFIRM");
        order.setConfirmedAt(Instant.now());
        applyStatus(order, Status.CONFIRMED, null);
        notifier.onConfirmed(order);
    }

    @Transactional
    public void reject(Order order, User actor, String reason) {
        guard.requireLegal(order, "REJECT");
        order.setCancelledAt(Instant.now());
        applyStatus(order, Status.CANCELLED, reason);
        stockRestorer.restore(order);
        refundCreator.create(order, actor, reason);
        notifier.onCancelled(order, reason);
    }

    @Transactional
    public void ship(Order order, ShipRequest req) {
        guard.requireLegal(order, "SHIP");
        order.setCourier(req.courier());
        order.setTrackingNo(req.trackingNo());
        order.setShippedAt(Instant.now());
        applyStatus(order, Status.SHIPPED, null);
        notifier.onShipped(order);
    }

    @Transactional
    public void readyMeetup(Order order, MeetupRequest req) {
        guard.requireLegal(order, "READY_MEETUP");
        order.setMeetupNote(req.meetupNote());
        order.setShippedAt(Instant.now()); // reuse shippedAt as fulfilment readiness timestamp
        applyStatus(order, Status.READY_FOR_MEETUP, null);
        notifier.onShipped(order);
    }

    @Transactional
    public void confirmReceipt(Order order) {
        guard.requireLegal(order, "CONFIRM_RECEIPT");
        order.setCompletedAt(Instant.now());
        applyStatus(order, Status.COMPLETED, null);
        notifier.onCompleted(order);
    }

    @Transactional
    public void requestRefund(Order order, User actor, String reason) {
        guard.requireLegal(order, "REFUND_REQUEST");
        applyStatus(order, Status.REFUND_REQUESTED, null);
        refundCreator.create(order, actor, reason);
        notifier.onRefundRequested(order);
    }

    /** Called by AutoCompleteJob — same as confirmReceipt but skips guard for scheduled path. */
    @Transactional
    public void autoComplete(Order order) {
        guard.requireLegal(order, "CONFIRM_RECEIPT");
        order.setCompletedAt(Instant.now());
        applyStatus(order, Status.COMPLETED, null);
        notifier.onCompleted(order);
        log.info("[AUTO-COMPLETE] orderNo={}", order.getOrderNo());
    }

    /** Called by PaymentSettleService (spec 07) — direct status stamp, no guard. */
    @Transactional
    public void markPaid(Long orderId) {
        orderRepo.findById(orderId).ifPresent(o -> {
            o.setStatus(Status.PAID);
            o.setUpdatedAt(Instant.now());
            orderRepo.save(o);
        });
    }

    private void applyStatus(Order order, Status next, String reason) {
        order.setStatus(next);
        if (reason != null) order.setCancelledReason(reason);
        order.setUpdatedAt(Instant.now());
        orderRepo.save(order);
    }
}
