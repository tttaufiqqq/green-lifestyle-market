package com.glm.checkout;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure fee math — no side effects, no I/O. Safe to property-test.
 * Platform fee = round_half_up(subtotal × feePercent%) to 2 d.p.
 * seller_net = subtotal − platformFee + shippingFee.
 */
@Component
public class FeeCalculator {

    private final int feePercent;

    public FeeCalculator(@Value("${app.platform-fee-percent:5}") int feePercent) {
        this.feePercent = feePercent;
    }

    public BigDecimal platformFee(BigDecimal subtotal) {
        return subtotal
            .multiply(BigDecimal.valueOf(feePercent))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal sellerNet(BigDecimal subtotal, BigDecimal platformFee, BigDecimal shippingFee) {
        return subtotal.subtract(platformFee).add(shippingFee);
    }

    /**
     * Convert RM amount to sen (integer, e.g. RM 45.90 → 4590).
     * Used only at the ToyyibPay boundary (docs/payments.md).
     */
    public long toSen(BigDecimal rmAmount) {
        return rmAmount
            .multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .longValue();
    }
}
