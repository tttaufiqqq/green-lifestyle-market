package com.glm.order;

import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.entity.Order;
import com.glm.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Order state machine — STUB for spec 07.
 * Spec 08 will complete all transitions (confirm, ship, meetup, complete, cancel).
 * Only transitions needed by the payment flow (PAID, EXPIRED) are implemented here;
 * those are delegated to PaymentSettleService which holds the full atomic logic.
 *
 * Guards: every legal transition is listed in domain-rules.md.
 * Anything not listed here throws E-ORD-STATE (409).
 */
@Service
public class OrderTransitionService {

    private static final Logger log = LoggerFactory.getLogger(OrderTransitionService.class);

    private final OrderRepository orderRepo;

    public OrderTransitionService(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    /**
     * Validates and applies a status transition.
     * Called by spec-08 controllers (confirm, ship, cancel, complete …).
     * Stub: only logs and throws E-ORD-STATE for now; spec 08 fills in the full machine.
     */
    @Transactional
    public void transition(Order order, String event) {
        log.warn("[OrderTransitionService STUB] event={} orderNo={} currentStatus={}",
            event, order.getOrderNo(), order.getStatus());
        throw new DomainException(ErrorCode.E_ORD_STATE,
            "Order transition '" + event + "' not yet implemented (spec 08)", 409);
    }

    // Convenience: direct status stamp used by PaymentSettleService internals only
    @Transactional
    public void markPaid(Long orderId) {
        orderRepo.findById(orderId).ifPresent(o -> {
            o.setStatus(Order.Status.PAID);
            o.setUpdatedAt(Instant.now());
            orderRepo.save(o);
        });
    }
}
