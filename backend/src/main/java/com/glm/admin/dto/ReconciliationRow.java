package com.glm.admin.dto;

import java.math.BigDecimal;

public record ReconciliationRow(
    String paymentNo,
    String billCode,
    String localStatus,
    boolean gatewayPaid,
    BigDecimal localAmountRm,
    String issue      // null if OK
) {}
