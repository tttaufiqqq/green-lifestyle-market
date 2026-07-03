package com.glm.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Full order detail DTO used by both buyer and seller views.
 * buyerPhone is null when caller is buyer or status is pre-CONFIRMED.
 * platformFee/sellerNet are null when caller is buyer.
 */
public record OrderDetailView(
    String orderNo,
    String status,
    String fulfilmentMethod,

    Long sellerId,
    String sellerName,
    Long buyerId,
    String buyerName,
    String buyerPhone,          // null unless caller=seller AND status ≥ CONFIRMED

    String shipName,
    String shipPhone,
    String shipAddress1,
    String shipAddress2,
    String shipPostcode,
    String shipCity,
    String shipState,
    String meetupLocation,
    String meetupNote,
    String courier,
    String trackingNo,

    BigDecimal subtotal,
    BigDecimal shippingFee,
    BigDecimal total,
    BigDecimal platformFee,     // null for buyer view
    BigDecimal sellerNet,       // null for buyer view

    List<OrderItemView> items,

    Instant createdAt,
    Instant confirmedAt,
    Instant shippedAt,
    Instant completedAt,
    Instant cancelledAt,
    String  cancelledReason,
    Instant autoCompleteAt,     // shippedAt+7d for SHIPPED/READY_FOR_MEETUP; else null

    String refundStatus         // Refund.Status name, or null
) {}
