package com.glm.checkout;

import com.glm.cart.entity.CartItem;
import com.glm.cart.repository.CartItemRepository;
import com.glm.cart.repository.CartRepository;
import com.glm.checkout.dto.*;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.payment.ToyyibPayClient;
import com.glm.payment.entity.Payment;
import com.glm.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    private final CartRepository     cartRepo;
    private final CartItemRepository cartItemRepo;
    private final CheckoutValidator  validator;
    private final FeeCalculator      feeCalc;
    private final CheckoutRecorder   recorder;
    private final ToyyibPayClient    toyyibPay;

    @Value("${app.payment-expiry-minutes:30}") private int expiryMinutes;
    @Value("${app.base-url}")                   private String appBaseUrl;

    public CheckoutService(CartRepository cartRepo, CartItemRepository cartItemRepo,
                            CheckoutValidator validator, FeeCalculator feeCalc,
                            CheckoutRecorder recorder, ToyyibPayClient toyyibPay) {
        this.cartRepo     = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.validator    = validator;
        this.feeCalc      = feeCalc;
        this.recorder     = recorder;
        this.toyyibPay    = toyyibPay;
    }

    /** Read-only preview: computes per-seller totals without writing anything. */
    @Transactional(readOnly = true)
    public PreviewResponse preview(User buyer, CheckoutRequest req) {
        validator.requireEmailVerified(buyer);

        var cart = cartRepo.findByBuyerId(buyer.getId())
            .orElseThrow(() -> new DomainException(ErrorCode.E_CHK_EMPTY, "Cart is empty", 422));
        List<CartItem> items = cartItemRepo.findByCartId(cart.getId());
        if (items.isEmpty())
            throw new DomainException(ErrorCode.E_CHK_EMPTY, "Cart is empty", 422);

        Map<Long, FulfilmentChoice> choices = req.fulfilments().stream()
            .collect(Collectors.toMap(FulfilmentChoice::sellerId, fc -> fc));

        // Group by seller (preserve insertion order)
        Map<Long, List<CartItem>> bySeller = new LinkedHashMap<>();
        for (CartItem ci : items)
            bySeller.computeIfAbsent(ci.getProduct().getSeller().getId(),
                k -> new ArrayList<>()).add(ci);

        List<OrderPreview> previews = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (var e : bySeller.entrySet()) {
            Long sellerId = e.getKey();
            List<CartItem> group = e.getValue();
            FulfilmentChoice fc = choices.get(sellerId);
            String method = fc != null ? fc.method() : "MEETUP";

            BigDecimal sub = BigDecimal.ZERO, ship = BigDecimal.ZERO;
            for (CartItem ci : group) {
                var p = ci.getProduct();
                sub = sub.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
                if ("SHIPPING".equals(method) && p.getShippingFee() != null)
                    ship = ship.add(p.getShippingFee());
            }
            BigDecimal fee = feeCalc.platformFee(sub);
            BigDecimal total = sub.add(ship);
            grandTotal = grandTotal.add(total);

            String sellerName = group.get(0).getProduct().getSeller().getName();
            previews.add(new OrderPreview(sellerId, sellerName, method,
                sub, ship, fee, feeCalc.sellerNet(sub, fee, ship), total));
        }

        validator.requireMinAmount(grandTotal);
        return new PreviewResponse(previews, grandTotal);
    }

    /**
     * Atomic checkout: lock → validate stock → create payment+orders+reservations →
     * call ToyyibPay createBill → return payment URL.
     * The DB transaction commits before the external HTTP call to avoid long-held locks.
     */
    public CheckoutResponse checkout(User buyer, CheckoutRequest req) {
        validator.requireEmailVerified(buyer);
        validator.requireShippingAddresses(req.fulfilments());

        Payment payment = recorder.createPaymentWithOrders(buyer, req, feeCalc, expiryMinutes);

        String billCode;
        try {
            long amountSen = feeCalc.toSen(payment.getAmount());
            billCode = toyyibPay.createBill(
                payment.getPaymentNo(),
                buyer.getName(), buyer.getEmail(), buyer.getPhone(),
                amountSen, appBaseUrl);
        } catch (Exception ex) {
            recorder.markFailed(payment.getId());
            throw new DomainException(ErrorCode.E_PAY_GATEWAY,
                "Payment provider unavailable, please try again", 502);
        }

        recorder.storeBillCode(payment.getId(), billCode);
        return new CheckoutResponse(payment.getPaymentNo(), billCode,
            toyyibPay.getPaymentUrl(billCode));
    }
}
