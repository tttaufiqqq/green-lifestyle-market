package com.glm.payment.dto;

/** Parsed form-encoded fields from ToyyibPay server-to-server callback. */
public record CallbackParams(
    String refno,
    String status,          // "1"=success, "2"=pending, "3"=fail
    String reason,
    String billcode,
    String orderId,         // = our paymentNo (billExternalReferenceNo)
    String amount,
    String transactionTime
) {}
