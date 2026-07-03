package com.glm.refund.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RefundView(
    Long id,
    String orderNo,
    String buyerName,
    String sellerName,
    BigDecimal amount,
    String status,
    String reason,
    String adminNote,
    String bankRef,
    Instant createdAt,
    Instant processedAt
) {}
