package com.glm.checkout.dto;

/** Returned by POST /checkout — redirect the buyer to paymentUrl. */
public record CheckoutResponse(
    String paymentNo,
    String billCode,
    String paymentUrl
) {}
