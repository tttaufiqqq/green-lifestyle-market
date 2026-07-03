package com.glm.payment.dto;

import java.util.List;

/** Polled by the SPA payment-result page until terminal status. */
public record PaymentStatusResponse(
    String       paymentNo,
    String       status,
    List<String> orderNos
) {}
