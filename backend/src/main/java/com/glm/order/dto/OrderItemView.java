package com.glm.order.dto;

import java.math.BigDecimal;

public record OrderItemView(
    Long productId,
    String titleSnapshot,
    String conditionSnapshot,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal lineTotal
) {}
