package com.glm.order;

import com.glm.common.security.GlmUserDetails;
import com.glm.order.dto.CancelRequest;
import com.glm.order.dto.MeetupRequest;
import com.glm.order.dto.OrderDetailView;
import com.glm.order.dto.OrderSummary;
import com.glm.order.dto.ShipRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Seller order endpoints: GET /me/sales, GET /me/sales/{orderNo},
 * POST confirm, reject, ship, ready-meetup.
 */
@RestController
@RequestMapping("/api/v1/me/sales")
public class SellerOrderController {

    private final SellerOrderService service;

    public SellerOrderController(SellerOrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderSummary> list(
        @AuthenticationPrincipal GlmUserDetails principal,
        @RequestParam(required = false) String tab) {
        return service.getSales(principal.getUser(), tab);
    }

    @GetMapping("/{orderNo}")
    public OrderDetailView detail(
        @AuthenticationPrincipal GlmUserDetails principal,
        @PathVariable String orderNo) {
        return service.getSale(principal.getUser(), orderNo);
    }

    @PostMapping("/{orderNo}/confirm")
    public ResponseEntity<Void> confirm(
        @AuthenticationPrincipal GlmUserDetails principal,
        @PathVariable String orderNo) {
        service.confirm(principal.getUser(), orderNo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderNo}/reject")
    public ResponseEntity<Void> reject(
        @AuthenticationPrincipal GlmUserDetails principal,
        @PathVariable String orderNo,
        @RequestBody(required = false) CancelRequest req) {
        service.reject(principal.getUser(), orderNo, req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderNo}/ship")
    public ResponseEntity<Void> ship(
        @AuthenticationPrincipal GlmUserDetails principal,
        @PathVariable String orderNo,
        @Valid @RequestBody ShipRequest req) {
        service.ship(principal.getUser(), orderNo, req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderNo}/ready-meetup")
    public ResponseEntity<Void> readyMeetup(
        @AuthenticationPrincipal GlmUserDetails principal,
        @PathVariable String orderNo,
        @Valid @RequestBody MeetupRequest req) {
        service.readyMeetup(principal.getUser(), orderNo, req);
        return ResponseEntity.noContent().build();
    }
}
