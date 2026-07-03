package com.glm.checkout;

import com.glm.checkout.dto.FulfilmentChoice;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.user.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Pre-condition guards run before the atomic checkout transaction. */
@Component
public class CheckoutValidator {

    private static final BigDecimal MINIMUM = BigDecimal.ONE; // RM1.00

    /** Buyer must have verified email to check out (domain-rules.md). */
    void requireEmailVerified(User buyer) {
        if (buyer.getEmailVerifiedAt() == null) {
            throw new DomainException(ErrorCode.E_AUTH_VERIFY,
                "Please verify your email before checking out", 422);
        }
    }

    /** Grand total must meet ToyyibPay FPX minimum (E-CHK-MIN). */
    void requireMinAmount(BigDecimal grandTotal) {
        if (grandTotal.compareTo(MINIMUM) < 0) {
            throw new DomainException(ErrorCode.E_CHK_MIN,
                "Total must be at least RM1.00", 422);
        }
    }

    /**
     * If SHIPPING is chosen for any group, all required address fields must be present
     * (E-CHK-ADDRESS). Validated here so we fail fast before touching the DB.
     */
    void requireShippingAddresses(List<FulfilmentChoice> fulfilments) {
        for (FulfilmentChoice fc : fulfilments) {
            if ("SHIPPING".equals(fc.method())) {
                if (isBlank(fc.shipName())    || isBlank(fc.shipPhone()) ||
                    isBlank(fc.shipAddress1()) || isBlank(fc.shipPostcode()) ||
                    isBlank(fc.shipCity())     || isBlank(fc.shipState())) {
                    throw new DomainException(ErrorCode.E_CHK_ADDRESS,
                        "Shipping address is required for seller " + fc.sellerId(), 400);
                }
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
