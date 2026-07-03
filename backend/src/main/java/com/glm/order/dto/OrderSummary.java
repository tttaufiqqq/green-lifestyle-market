package com.glm.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummary(
    String orderNo,
    String counterpartName,  // seller name for buyer, buyer name for seller
    BigDecimal total,
    String status,
    String fulfilmentMethod,
    Instant createdAt
) {}
