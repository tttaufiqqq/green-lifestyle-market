package com.glm.order;

import com.glm.common.security.GlmUserDetails;
import com.glm.order.dto.CancelRequest;
import com.glm.order.dto.OrderDetailView;
import com.glm.order.dto.OrderSummary;
import com.glm.order.dto.RefundRequestBody;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Buyer order endpoints: GET /me/orders, GET /me/orders/{orderNo},
 * POST cancel, confirm-receipt, refund-request.
 */
@RestController
@RequestMapping("/api/v1/me/orders")
public class BuyerOrderController {

    private final BuyerOrderService service;

    public BuyerOrderController(BuyerOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderSummary> list(
        @AuthenticationPrincipal GlmUserDetails principal,
        @RequestParam(required = false) String tab) {
        return service.getOrders(principal.getUser(), tab);
    }

    @GetMapping("/{orderNo}")
    public OrderDetailView detail(
        @AuthenticationPrincipal GlmUserDetails principal,
        @PathVariable String orderNo) {
        return service.getOrder(principal.getUser(), orderNo);
    }

    @PostMapping("/{orderNo}/cancel")
    public ResponseEntity<Void> cancel(
        @AuthenticationPrincipal GlmUserDetails principal,
        @PathVariable String orderNo,
        @RequestBody(required = false) CancelRequest req) {
        service.cancel(principal.getUser(), orderNo, req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderNo}/confirm-receipt")
    public ResponseEntity<Void> confirmReceipt(
        @AuthenticationPrincipal GlmUserDetails principal,
        @PathVariable String orderNo) {
        service.confirmReceipt(principal.getUser(), orderNo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderNo}/refund-request")
    public ResponseEntity<Void> refundRequest(
        @AuthenticationPrincipal GlmUserDetails principal,
        @PathVariable String orderNo,
        @Valid @RequestBody RefundRequestBody req) {
        service.requestRefund(principal.getUser(), orderNo, req);
        return ResponseEntity.noContent().build();
    }
}
