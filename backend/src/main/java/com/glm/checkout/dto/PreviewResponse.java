package com.glm.checkout.dto;

import java.math.BigDecimal;
import java.util.List;

/** Read-only cart preview: per-seller order summaries + grand total. No side effects. */
public record PreviewResponse(
    List<OrderPreview> orders,
    BigDecimal         grandTotal
) {}
