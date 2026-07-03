package com.glm.admin.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardView(
    long userCount,
    long activeListings,
    BigDecimal escrowHeld,
    BigDecimal platformFeesMtd,
    Map<String, Long> ordersByStatus,
    long reviewPayments,
    long pendingRefunds,
    long pendingPayouts
) {}
