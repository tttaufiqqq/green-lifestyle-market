package com.glm.payment;

import com.glm.catalog.entity.Product;
import com.glm.catalog.repository.ProductRepository;
import com.glm.notification.entity.Notification;
import com.glm.notification.repository.NotificationRepository;
import com.glm.order.entity.Order;
import com.glm.order.entity.StockReservation;
import com.glm.order.repository.OrderRepository;
import com.glm.order.repository.StockReservationRepository;
import com.glm.payment.entity.Payment;
import com.glm.payment.repository.PaymentRepository;
import com.glm.user.entity.User;
import com.glm.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Atomic payment state transitions called from PaymentCallbackService and PaymentSweeper.
 * Every method acquires a row-level lock on the payment first (prevents sweeper vs callback races).
 * Idempotent: re-checks status inside the TX; already-processed payments are no-ops.
 */
@Service
public class PaymentSettleService {

    private static final Logger log = LoggerFactory.getLogger(PaymentSettleService.class);

    private final PaymentRepository          paymentRepo;
    private final OrderRepository            orderRepo;
    private final StockReservationRepository reservationRepo;
    private final ProductRepository          productRepo;
    private final NotificationRepository     notificationRepo;
    private final UserRepository             userRepo;
    private final EntityManager              em;

    public PaymentSettleService(PaymentRepository paymentRepo, OrderRepository orderRepo,
                                 StockReservationRepository reservationRepo,
                                 ProductRepository productRepo,
                                 NotificationRepository notificationRepo,
                                 UserRepository userRepo,
                                 EntityManager em) {
        this.paymentRepo     = paymentRepo;
        this.orderRepo       = orderRepo;
        this.reservationRepo = reservationRepo;
        this.productRepo     = productRepo;
        this.notificationRepo = notificationRepo;
        this.userRepo        = userRepo;
        this.em              = em;
    }

    /** UC-07 R07-4: payment→SUCCESS, orders→PAID, reservations CONSUMED, qty decremented. */
    @Transactional
    public void settlePayment(Long paymentId, String refNo) {
        Payment payment = lockPayment(paymentId);
        if (payment == null || payment.getStatus() != Payment.Status.PENDING) return;

        Instant now = Instant.now();
        payment.setStatus(Payment.Status.SUCCESS);
        payment.setToyyibpayRefNo(refNo);
        payment.setVerifiedAt(now);
        payment.setPaidAt(now);
        payment.setUpdatedAt(now);
        paymentRepo.save(payment);

        List<Order> orders = orderRepo.findByPaymentId(paymentId);
        for (Order order : orders) {
            order.setStatus(Order.Status.PAID);
            order.setUpdatedAt(now);
            orderRepo.save(order);

            for (StockReservation r : reservationRepo.findByOrderId(order.getId())) {
                r.setStatus(StockReservation.Status.CONSUMED);
                reservationRepo.save(r);

                Product p = r.getProduct();
                int newQty = Math.max(0, p.getQuantity() - r.getQuantity());
                p.setQuantity(newQty);
                if (newQty == 0 && p.getStatus() == Product.Status.ACTIVE)
                    p.setStatus(Product.Status.SOLD_OUT);
                productRepo.save(p);
            }
        }

        // Notification stubs (outbox rows; push delivery wired in spec 10)
        notify(payment.getBuyer(), "PAYMENT_RECEIVED", "Payment confirmed",
            "Your payment has been received. Order is now being processed.",
            payment.getPaymentNo());
        for (Order o : orders) {
            notify(o.getSeller(), "NEW_ORDER_PAID", "New order received",
                "You have a new paid order " + o.getOrderNo() + ". Please confirm soon.",
                o.getOrderNo());
        }
        log.info("[SETTLE] paymentId={} paymentNo={} orders={}", paymentId,
            payment.getPaymentNo(), orders.stream().map(Order::getOrderNo).toList());
    }

    /** UC-08 R08-2: payment→EXPIRED, orders→EXPIRED, reservations RELEASED. */
    @Transactional
    public void expirePayment(Long paymentId) {
        Payment payment = lockPayment(paymentId);
        if (payment == null || payment.getStatus() != Payment.Status.PENDING) return;

        Instant now = Instant.now();
        payment.setStatus(Payment.Status.EXPIRED);
        payment.setUpdatedAt(now);
        paymentRepo.save(payment);

        orderRepo.findByPaymentId(paymentId).forEach(o -> {
            o.setStatus(Order.Status.EXPIRED);
            o.setUpdatedAt(now);
            orderRepo.save(o);
            reservationRepo.findByOrderId(o.getId()).forEach(r -> {
                r.setStatus(StockReservation.Status.RELEASED);
                reservationRepo.save(r);
            });
        });

        notify(payment.getBuyer(), "PAYMENT_EXPIRED", "Checkout expired",
            "Your payment session expired. Please return to cart and try again.",
            payment.getPaymentNo());
        log.info("[EXPIRE] paymentId={}", paymentId);
    }

    /** R07-E1: set to REVIEW when verification fails (amount mismatch or API error); alerts admins. */
    @Transactional
    public void markReview(Long paymentId) {
        Payment payment = lockPayment(paymentId);
        if (payment != null && payment.getStatus() == Payment.Status.PENDING) {
            payment.setStatus(Payment.Status.REVIEW);
            payment.setUpdatedAt(Instant.now());
            paymentRepo.save(payment);
            alertAdmins(payment);
            log.warn("[REVIEW] paymentId={} — manual admin check required", paymentId);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Payment lockPayment(Long paymentId) {
        var list = em.createQuery("SELECT p FROM Payment p WHERE p.id = :id", Payment.class)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .setParameter("id", paymentId)
            .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private void alertAdmins(Payment payment) {
        userRepo.findAll().stream()
            .filter(u -> u.getRole() == User.Role.ADMIN)
            .forEach(admin -> notify(admin, "ADMIN_ALERT", "Payment needs review",
                "Payment " + payment.getPaymentNo() + " failed verification and needs manual review.",
                payment.getPaymentNo()));
    }

    private void notify(User user, String type, String title, String body, String ref) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setData("{\"ref\":\"" + ref + "\"}");
        n.setCreatedAt(Instant.now());
        notificationRepo.save(n);
        log.info("[NOTIFY-STUB] type={} userId={} ref={}", type, user.getId(), ref);
    }
}
