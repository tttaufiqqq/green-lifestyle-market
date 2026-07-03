package com.glm.payout;

import com.glm.common.security.GlmUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Seller-facing payout view: eligible orders + payout history.
 * GET /me/payouts
 */
@RestController
@RequestMapping("/api/v1/me/payouts")
public class PayoutController {

    private final PayoutService service;

    public PayoutController(PayoutService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, Object> myPayouts(@AuthenticationPrincipal GlmUserDetails principal) {
        return service.sellerPayoutsView(principal.getUser());
    }
}
