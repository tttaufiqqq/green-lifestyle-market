package com.glm.payout.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PayoutView(
    Long id,
    String payoutNo,
    String sellerName,
    BigDecimal amount,
    String status,
    String bankRef,
    Instant paidAt,
    Instant createdAt,
    List<PayoutItemRow> items
) {
    public record PayoutItemRow(String orderNo, BigDecimal amount) {}
}
