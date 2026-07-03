package com.glm.admin;

import com.glm.common.security.GlmUserDetails;
import com.glm.payout.PayoutService;
import com.glm.payout.dto.CreatePayoutRequest;
import com.glm.payout.dto.EligibleSellerView;
import com.glm.payout.dto.MarkPaidRequest;
import com.glm.payout.dto.PayoutView;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/payouts")
public class AdminPayoutController {

    private final PayoutService service;

    public AdminPayoutController(PayoutService service) {
        this.service = service;
    }

    @GetMapping("/eligible")
    public List<EligibleSellerView> eligible() {
        return service.eligibleBySeller();
    }

    @GetMapping("/pending")
    public List<PayoutView> pending() {
        return service.listPending();
    }

    @PostMapping
    public ResponseEntity<PayoutView> create(
        @Valid @RequestBody CreatePayoutRequest req,
        @AuthenticationPrincipal GlmUserDetails principal) {
        PayoutView view = service.create(req, principal.getUser());
        return ResponseEntity.created(URI.create("/api/v1/admin/payouts/" + view.id())).body(view);
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<Void> markPaid(
        @PathVariable Long id,
        @Valid @RequestBody MarkPaidRequest req,
        @AuthenticationPrincipal GlmUserDetails principal) {
        service.markPaid(id, req.bankRef(), principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
