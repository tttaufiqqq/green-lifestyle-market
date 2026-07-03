package com.glm.order.lifecycle;

import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.entity.Order;
import com.glm.order.entity.Order.FulfilmentMethod;
import com.glm.order.entity.Order.Status;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Pure transition table: (currentStatus × event) → nextStatus.
 * Throws E-ORD-STATE 409 for any event not in the table.
 * Also enforces method-specific constraints (SHIP vs READY_MEETUP).
 */
@Component
public class TransitionGuard {

    private static final Map<Status, Map<String, Status>> TABLE = Map.of(
        Status.PENDING_PAYMENT, Map.of(
            "BUYER_CANCEL", Status.EXPIRED
        ),
        Status.PAID, Map.of(
            "CONFIRM",      Status.CONFIRMED,
            "REJECT",       Status.CANCELLED,
            "BUYER_CANCEL", Status.CANCELLED
        ),
        Status.CONFIRMED, Map.of(
            "SHIP",           Status.SHIPPED,
            "READY_MEETUP",   Status.READY_FOR_MEETUP,
            "REFUND_REQUEST", Status.REFUND_REQUESTED
        ),
        Status.SHIPPED, Map.of(
            "CONFIRM_RECEIPT", Status.COMPLETED,
            "REFUND_REQUEST",  Status.REFUND_REQUESTED
        ),
        Status.READY_FOR_MEETUP, Map.of(
            "CONFIRM_RECEIPT", Status.COMPLETED,
            "REFUND_REQUEST",  Status.REFUND_REQUESTED
        )
    );

    /**
     * Returns the next status, or throws E-ORD-STATE (409) if the event is illegal.
     */
    public Status requireLegal(Order order, String event) {
        Map<String, Status> events = TABLE.get(order.getStatus());
        if (events == null || !events.containsKey(event)) {
            throw new DomainException(ErrorCode.E_ORD_STATE,
                "Event '" + event + "' is not allowed when order is " + order.getStatus(), 409);
        }
        if ("SHIP".equals(event) && order.getFulfilmentMethod() != FulfilmentMethod.SHIPPING) {
            throw new DomainException(ErrorCode.E_ORD_STATE,
                "SHIP is only allowed on shipping orders", 409);
        }
        if ("READY_MEETUP".equals(event) && order.getFulfilmentMethod() != FulfilmentMethod.MEETUP) {
            throw new DomainException(ErrorCode.E_ORD_STATE,
                "READY_MEETUP is only allowed on meetup orders", 409);
        }
        return events.get(event);
    }
}
