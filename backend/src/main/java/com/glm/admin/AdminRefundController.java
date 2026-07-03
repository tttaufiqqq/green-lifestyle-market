package com.glm.admin;

import com.glm.common.security.GlmUserDetails;
import com.glm.refund.RefundService;
import com.glm.refund.dto.ProcessRefundRequest;
import com.glm.refund.dto.RejectRefundRequest;
import com.glm.refund.dto.RefundView;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/refunds")
public class AdminRefundController {

    private final RefundService service;

    public AdminRefundController(RefundService service) {
        this.service = service;
    }

    @GetMapping
    public List<RefundView> list(@RequestParam(required = false) String status) {
        return service.list(status);
    }

    @GetMapping("/{id}")
    public RefundView get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
        @PathVariable Long id,
        @AuthenticationPrincipal GlmUserDetails principal) {
        service.approve(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
        @PathVariable Long id,
        @Valid @RequestBody RejectRefundRequest req,
        @AuthenticationPrincipal GlmUserDetails principal) {
        service.reject(id, principal.getUser(), req.adminNote());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<Void> process(
        @PathVariable Long id,
        @Valid @RequestBody ProcessRefundRequest req,
        @AuthenticationPrincipal GlmUserDetails principal) {
        service.process(id, principal.getUser(), req.bankRef(), req.adminNote());
        return ResponseEntity.noContent().build();
    }
}
