package com.glm.checkout;

import com.glm.cart.entity.CartItem;
import com.glm.cart.repository.CartItemRepository;
import com.glm.cart.repository.CartRepository;
import com.glm.catalog.entity.Product;
import com.glm.checkout.dto.CheckoutRequest;
import com.glm.checkout.dto.FulfilmentChoice;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.entity.Order;
import com.glm.order.entity.OrderItem;
import com.glm.order.entity.StockReservation;
import com.glm.order.repository.OrderItemRepository;
import com.glm.order.repository.OrderRepository;
import com.glm.order.repository.StockReservationRepository;
import com.glm.payment.entity.Payment;
import com.glm.payment.repository.PaymentRepository;
import com.glm.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Atomic checkout transaction: lock products FOR UPDATE → validate stock →
 * create payment + orders + reservations → clear cart items.
 * Called from CheckoutService (non-transactional orchestrator) so that
 * the ToyyibPay HTTP call happens outside this transaction.
 */
@Service
public class CheckoutRecorder {

    private final CartRepository             cartRepo;
    private final CartItemRepository         cartItemRepo;
    private final PaymentRepository          paymentRepo;
    private final OrderRepository            orderRepo;
    private final OrderItemRepository        orderItemRepo;
    private final StockReservationRepository reservationRepo;
    private final EntityManager              em;

    public CheckoutRecorder(CartRepository cartRepo, CartItemRepository cartItemRepo,
                             PaymentRepository paymentRepo, OrderRepository orderRepo,
                             OrderItemRepository orderItemRepo,
                             StockReservationRepository reservationRepo, EntityManager em) {
        this.cartRepo       = cartRepo;
        this.cartItemRepo   = cartItemRepo;
        this.paymentRepo    = paymentRepo;
        this.orderRepo      = orderRepo;
        this.orderItemRepo  = orderItemRepo;
        this.reservationRepo = reservationRepo;
        this.em             = em;
    }

    private record Totals(BigDecimal subtotal, BigDecimal shipping,
                          BigDecimal fee, BigDecimal net, BigDecimal total) {}

    @Transactional
    public Payment createPaymentWithOrders(User buyer, CheckoutRequest req,
                                            FeeCalculator feeCalc, int expiryMinutes) {
        var cart  = cartRepo.findByBuyerId(buyer.getId())
            .orElseThrow(() -> new DomainException(ErrorCode.E_CHK_EMPTY, "Cart is empty", 422));
        List<CartItem> items = cartItemRepo.findByCartId(cart.getId());
        if (items.isEmpty())
            throw new DomainException(ErrorCode.E_CHK_EMPTY, "Cart is empty", 422);

        List<Long> pids = items.stream().map(ci -> ci.getProduct().getId()).toList();
        Map<Long, Product> pm = lockProducts(pids);

        // Collect all stock failures before throwing (better UX)
        List<String> errors = new ArrayList<>();
        for (CartItem ci : items) {
            Product p = pm.get(ci.getProduct().getId());
            int avail = Math.max(0, p.getQuantity() - heldFor(p.getId()));
            if (ci.getQuantity() > avail)
                errors.add(p.getTitle() + " (only " + avail + " left)");
        }
        if (!errors.isEmpty())
            throw new DomainException(ErrorCode.E_CHK_STOCK,
                "Items changed while checking out: " + String.join(", ", errors), 409);

        Map<Long, List<CartItem>> bySeller = groupBySeller(items);
        Map<Long, FulfilmentChoice> choices = req.fulfilments().stream()
            .collect(Collectors.toMap(FulfilmentChoice::sellerId, fc -> fc));

        // Pass 1: compute totals per group to get the grand total for the payment
        Map<Long, Totals> totalsMap = new LinkedHashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (var e : bySeller.entrySet()) {
            Totals t = computeTotals(e.getValue(), choices.get(e.getKey()), pm, feeCalc);
            totalsMap.put(e.getKey(), t);
            grandTotal = grandTotal.add(t.total());
        }

        Instant now       = Instant.now();
        Instant expiresAt = now.plusSeconds(expiryMinutes * 60L);
        Payment payment   = paymentRepo.save(buildPayment(buyer, grandTotal, expiresAt, now));

        // Pass 2: create one order per seller group
        for (var e : bySeller.entrySet()) {
            Long sellerId = e.getKey();
            List<CartItem> group = e.getValue();
            User seller = group.get(0).getProduct().getSeller();
            FulfilmentChoice fc = choices.get(sellerId);
            Order order = orderRepo.save(buildOrder(buyer, seller, payment, fc, totalsMap.get(sellerId), now));
            createItems(order, group, pm);
            createReservations(order, group, pm, expiresAt, now);
        }
        cartItemRepo.deleteAll(items);
        return payment;
    }

    @Transactional
    public void storeBillCode(Long paymentId, String billCode) {
        paymentRepo.findById(paymentId).ifPresent(p -> {
            p.setToyyibpayBillCode(billCode);
            p.setUpdatedAt(Instant.now());
            paymentRepo.save(p);
        });
    }

    @Transactional
    public void markFailed(Long paymentId) {
        paymentRepo.findById(paymentId).ifPresent(p -> {
            if (p.getStatus() != Payment.Status.PENDING) return;
            p.setStatus(Payment.Status.FAILED);
            p.setUpdatedAt(Instant.now());
            paymentRepo.save(p);
            Instant now = Instant.now();
            orderRepo.findByPaymentId(paymentId).forEach(o -> {
                o.setStatus(Order.Status.EXPIRED);
                o.setUpdatedAt(now);
                orderRepo.save(o);
                reservationRepo.findByOrderId(o.getId()).forEach(r -> {
                    r.setStatus(StockReservation.Status.RELEASED);
                    reservationRepo.save(r);
                });
            });
        });
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private Totals computeTotals(List<CartItem> items, FulfilmentChoice fc,
                                  Map<Long, Product> pm, FeeCalculator feeCalc) {
        BigDecimal sub = BigDecimal.ZERO, ship = BigDecimal.ZERO;
        for (CartItem ci : items) {
            Product p = pm.get(ci.getProduct().getId());
            sub = sub.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            if ("SHIPPING".equals(fc != null ? fc.method() : null) && p.getShippingFee() != null)
                ship = ship.add(p.getShippingFee());
        }
        BigDecimal fee = feeCalc.platformFee(sub);
        return new Totals(sub, ship, fee, feeCalc.sellerNet(sub, fee, ship), sub.add(ship));
    }

    private Payment buildPayment(User buyer, BigDecimal amount, Instant expiresAt, Instant now) {
        Payment p = new Payment();
        p.setPaymentNo(generateNo("PAY-"));
        p.setBuyer(buyer);
        p.setAmount(amount);
        p.setStatus(Payment.Status.PENDING);
        p.setExpiresAt(expiresAt);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        return p;
    }

    private Order buildOrder(User buyer, User seller, Payment payment,
                              FulfilmentChoice fc, Totals t, Instant now) {
        boolean ship = fc != null && "SHIPPING".equals(fc.method());
        Order o = new Order();
        o.setOrderNo(generateNo("ORD-"));
        o.setPayment(payment);
        o.setBuyer(buyer);
        o.setSeller(seller);
        o.setStatus(Order.Status.PENDING_PAYMENT);
        o.setFulfilmentMethod(ship ? Order.FulfilmentMethod.SHIPPING : Order.FulfilmentMethod.MEETUP);
        if (ship && fc != null) {
            o.setShipName(fc.shipName());     o.setShipPhone(fc.shipPhone());
            o.setShipAddress1(fc.shipAddress1()); o.setShipAddress2(fc.shipAddress2());
            o.setShipPostcode(fc.shipPostcode()); o.setShipCity(fc.shipCity());
            o.setShipState(fc.shipState());
        }
        o.setSubtotal(t.subtotal()); o.setShippingFee(t.shipping());
        o.setTotal(t.total());       o.setPlatformFee(t.fee()); o.setSellerNet(t.net());
        o.setCreatedAt(now);         o.setUpdatedAt(now);
        return o;
    }

    private void createItems(Order order, List<CartItem> items, Map<Long, Product> pm) {
        for (CartItem ci : items) {
            Product p = pm.get(ci.getProduct().getId());
            OrderItem oi = new OrderItem();
            oi.setOrder(order); oi.setProduct(p);
            oi.setTitleSnapshot(p.getTitle());
            oi.setConditionSnapshot(p.getItemCondition().name());
            oi.setUnitPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setLineTotal(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            orderItemRepo.save(oi);
        }
    }

    private void createReservations(Order order, List<CartItem> items,
                                     Map<Long, Product> pm, Instant expiresAt, Instant now) {
        for (CartItem ci : items) {
            StockReservation r = new StockReservation();
            r.setProduct(pm.get(ci.getProduct().getId()));
            r.setOrder(order);
            r.setQuantity(ci.getQuantity());
            r.setStatus(StockReservation.Status.HELD);
            r.setExpiresAt(expiresAt);
            r.setCreatedAt(now);
            reservationRepo.save(r);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Product> lockProducts(List<Long> ids) {
        return em.createQuery("SELECT p FROM Product p WHERE p.id IN :ids", Product.class)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .setParameter("ids", ids)
            .getResultList()
            .stream().collect(Collectors.toMap(Product::getId, p -> p));
    }

    private int heldFor(Long productId) {
        Number n = (Number) em.createQuery(
            "SELECT COALESCE(SUM(sr.quantity),0) FROM StockReservation sr " +
            "WHERE sr.product.id=:pid AND sr.status=:held")
            .setParameter("pid", productId)
            .setParameter("held", StockReservation.Status.HELD)
            .getSingleResult();
        return n.intValue();
    }

    private static Map<Long, List<CartItem>> groupBySeller(List<CartItem> items) {
        Map<Long, List<CartItem>> map = new LinkedHashMap<>();
        for (CartItem ci : items)
            map.computeIfAbsent(ci.getProduct().getSeller().getId(), k -> new ArrayList<>()).add(ci);
        return map;
    }

    private static String generateNo(String prefix) {
        String yymm = YearMonth.now().format(DateTimeFormatter.ofPattern("yyMM"));
        return prefix + yymm + "-" + String.format("%06d", (int)(Math.random() * 1_000_000));
    }
}
