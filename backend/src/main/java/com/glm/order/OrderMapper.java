package com.glm.order;

import com.glm.order.dto.OrderDetailView;
import com.glm.order.dto.OrderItemView;
import com.glm.order.dto.OrderSummary;
import com.glm.order.entity.Order;
import com.glm.order.entity.Order.Status;
import com.glm.order.repository.OrderItemRepository;
import com.glm.refund.entity.Refund;
import com.glm.refund.repository.RefundRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class OrderMapper {

    private final OrderItemRepository itemRepo;
    private final RefundRepository refundRepo;

    public OrderMapper(OrderItemRepository itemRepo, RefundRepository refundRepo) {
        this.itemRepo   = itemRepo;
        this.refundRepo = refundRepo;
    }

    public OrderSummary toSummary(Order order, boolean buyerPerspective) {
        String counterpart = buyerPerspective
            ? order.getSeller().getName()
            : order.getBuyer().getName();
        return new OrderSummary(
            order.getOrderNo(), counterpart, order.getTotal(),
            order.getStatus().name(), order.getFulfilmentMethod().name(),
            order.getCreatedAt());
    }

    public OrderDetailView toDetail(Order order, boolean buyerPerspective, int autoCompleteDays) {
        List<OrderItemView> items = itemRepo.findByOrderId(order.getId()).stream()
            .map(i -> new OrderItemView(
                i.getProduct().getId(), i.getTitleSnapshot(), i.getConditionSnapshot(),
                i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
            .toList();

        String refundStatus = refundRepo.findByOrderId(order.getId())
            .map(r -> r.getStatus().name()).orElse(null);

        boolean revealPhone = !buyerPerspective && isConfirmedOrLater(order.getStatus());
        Instant autoCompleteAt = computeAutoCompleteAt(order, autoCompleteDays);

        return new OrderDetailView(
            order.getOrderNo(), order.getStatus().name(), order.getFulfilmentMethod().name(),
            order.getSeller().getId(), order.getSeller().getName(),
            order.getBuyer().getId(), order.getBuyer().getName(),
            revealPhone ? order.getBuyer().getPhone() : null,
            order.getShipName(), order.getShipPhone(),
            order.getShipAddress1(), order.getShipAddress2(),
            order.getShipPostcode(), order.getShipCity(), order.getShipState(),
            order.getMeetupLocation(), order.getMeetupNote(),
            order.getCourier(), order.getTrackingNo(),
            order.getSubtotal(), order.getShippingFee(), order.getTotal(),
            buyerPerspective ? null : order.getPlatformFee(),
            buyerPerspective ? null : order.getSellerNet(),
            items,
            order.getCreatedAt(), order.getConfirmedAt(), order.getShippedAt(),
            order.getCompletedAt(), order.getCancelledAt(), order.getCancelledReason(),
            autoCompleteAt, refundStatus
        );
    }

    private boolean isConfirmedOrLater(Status status) {
        return switch (status) {
            case CONFIRMED, SHIPPED, READY_FOR_MEETUP,
                 COMPLETED, REFUND_REQUESTED, REFUNDED -> true;
            default -> false;
        };
    }

    private Instant computeAutoCompleteAt(Order order, int days) {
        if ((order.getStatus() == Status.SHIPPED || order.getStatus() == Status.READY_FOR_MEETUP)
            && order.getShippedAt() != null) {
            return order.getShippedAt().plus(days, ChronoUnit.DAYS);
        }
        return null;
    }
}
