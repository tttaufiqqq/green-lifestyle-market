package com.glm.payment;

import com.glm.common.security.GlmUserDetails;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.repository.OrderRepository;
import com.glm.payment.dto.CallbackParams;
import com.glm.payment.dto.PaymentStatusResponse;
import com.glm.payment.entity.Payment;
import com.glm.payment.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentRepository      paymentRepo;
    private final OrderRepository        orderRepo;
    private final PaymentCallbackService callbackService;
    private final CallbackRateLimiter    rateLimiter;

    public PaymentController(PaymentRepository paymentRepo, OrderRepository orderRepo,
                              PaymentCallbackService callbackService, CallbackRateLimiter rateLimiter) {
        this.paymentRepo     = paymentRepo;
        this.orderRepo       = orderRepo;
        this.callbackService = callbackService;
        this.rateLimiter     = rateLimiter;
    }

    /** Polled by the SPA payment-result page until a terminal state is reached. */
    @GetMapping("/{paymentNo}")
    public PaymentStatusResponse status(@AuthenticationPrincipal GlmUserDetails d,
                                         @PathVariable String paymentNo) {
        Payment payment = paymentRepo.findByPaymentNo(paymentNo)
            .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Payment not found", 404));
        if (!payment.getBuyer().getId().equals(d.getUser().getId()))
            throw new DomainException(ErrorCode.E_AUTH_OWN, "Not your payment", 403);

        List<String> orderNos = orderRepo.findByPaymentId(payment.getId())
            .stream().map(o -> o.getOrderNo()).toList();
        return new PaymentStatusResponse(payment.getPaymentNo(), payment.getStatus().name(), orderNos);
    }

    /**
     * Server-to-server callback from ToyyibPay (CSRF-exempt per SecurityConfig).
     * Always returns 200 OK regardless of processing result (gateway retry guard).
     * Rate limited per IP (CallbackRateLimiter) since this is public and unauthenticated.
     */
    @PostMapping("/toyyibpay/callback")
    public ResponseEntity<String> callback(
        HttpServletRequest httpReq,
        @RequestParam(required = false) String refno,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String reason,
        @RequestParam(required = false) String billcode,
        @RequestParam(value = "order_id",         required = false) String orderId,
        @RequestParam(required = false) String amount,
        @RequestParam(value = "transaction_time", required = false) String transactionTime
    ) {
        if (!rateLimiter.allow(httpReq.getRemoteAddr())) {
            log.warn("[CB-RATE-LIMIT] ip={}", httpReq.getRemoteAddr());
            return ResponseEntity.ok("OK");
        }
        try {
            callbackService.processCallback(
                new CallbackParams(refno, status, reason, billcode, orderId, amount, transactionTime));
        } catch (Exception e) {
            // Swallow — gateway must always receive 200 (duplicate delivery guard)
        }
        return ResponseEntity.ok("OK");
    }

    /**
     * Browser return from ToyyibPay. Triggers a verify attempt then redirects to
     * the SPA payment result page. Rate limited per IP like the callback endpoint.
     */
    @GetMapping("/toyyibpay/return")
    public ResponseEntity<Void> returnUrl(
        HttpServletRequest httpReq,
        @RequestParam(required = false) String billcode,
        @RequestParam(value = "order_id", required = false) String orderId,
        @RequestParam(value = "status_id", required = false) String statusId
    ) {
        String paymentNo = rateLimiter.allow(httpReq.getRemoteAddr())
            ? callbackService.processReturn(billcode, orderId)
            : (orderId != null ? orderId : "unknown");
        return ResponseEntity.status(302)
            .location(URI.create("/payment/result/" + paymentNo))
            .build();
    }
}
