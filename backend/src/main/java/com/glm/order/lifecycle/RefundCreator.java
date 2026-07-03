package com.glm.order.lifecycle;

import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.entity.Order;
import com.glm.refund.entity.Refund;
import com.glm.refund.repository.RefundRepository;
import com.glm.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Creates a Refund record in REQUESTED state for a cancelled/refund-requested order.
 * Throws E-REF-EXISTS (409) if a refund already exists for the same order.
 * Must be called inside an active @Transactional context.
 */
@Component
public class RefundCreator {

    private final RefundRepository refundRepo;

    public RefundCreator(RefundRepository refundRepo) {
        this.refundRepo = refundRepo;
    }

    public Refund create(Order order, User requestedBy, String reason) {
        refundRepo.findByOrderId(order.getId()).ifPresent(existing -> {
            throw new DomainException(ErrorCode.E_REF_EXISTS,
                "Refund already requested for order " + order.getOrderNo(), 409);
        });

        Instant now = Instant.now();
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setRequestedBy(requestedBy);
        refund.setReason(reason != null && !reason.isBlank() ? reason : "Cancelled");
        refund.setAmount(order.getTotal()); // full refund in v1
        refund.setStatus(Refund.Status.REQUESTED);
        refund.setCreatedAt(now);
        refund.setUpdatedAt(now);
        return refundRepo.save(refund);
    }
}
