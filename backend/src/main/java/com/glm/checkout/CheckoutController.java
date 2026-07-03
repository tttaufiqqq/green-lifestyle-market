package com.glm.checkout;

import com.glm.checkout.dto.CheckoutRequest;
import com.glm.checkout.dto.CheckoutResponse;
import com.glm.checkout.dto.PreviewResponse;
import com.glm.common.security.GlmUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /** No side effects — returns per-seller totals for display before the buyer commits. */
    @PostMapping("/preview")
    public PreviewResponse preview(@AuthenticationPrincipal GlmUserDetails d,
                                   @Valid @RequestBody CheckoutRequest req) {
        return checkoutService.preview(d.getUser(), req);
    }

    /** Atomic checkout: creates payment + orders, calls ToyyibPay, returns payment URL. */
    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(@AuthenticationPrincipal GlmUserDetails d,
                                                      @Valid @RequestBody CheckoutRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(checkoutService.checkout(d.getUser(), req));
    }
}
