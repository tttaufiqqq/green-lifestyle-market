package com.glm.payout.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record EligibleSellerView(
    Long sellerId,
    String sellerName,
    String bankName,
    String accountNo,
    boolean bankVerified,
    int orderCount,
    BigDecimal totalNet,
    List<EligibleOrderView> orders
) {
    public record EligibleOrderView(
        Long orderId,
        String orderNo,
        BigDecimal sellerNet,
        Instant completedAt
    ) {}
}
