package com.glm.checkout.dto;

import java.math.BigDecimal;

/** Financial summary for one seller group (one future order). */
public record OrderPreview(
    Long       sellerId,
    String     sellerName,
    String     fulfilmentMethod,
    BigDecimal subtotal,
    BigDecimal shippingFee,
    BigDecimal platformFee,
    BigDecimal sellerNet,
    BigDecimal total
) {}
