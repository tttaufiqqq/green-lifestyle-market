package com.glm.payout;

import com.glm.common.audit.AuditLog;
import com.glm.common.audit.AuditLogRepository;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.notification.entity.Notification;
import com.glm.notification.repository.NotificationRepository;
import com.glm.order.entity.Order;
import com.glm.order.repository.OrderRepository;
import com.glm.payout.dto.CreatePayoutRequest;
import com.glm.payout.dto.EligibleSellerView;
import com.glm.payout.dto.EligibleSellerView.EligibleOrderView;
import com.glm.payout.dto.PayoutView;
import com.glm.payout.dto.PayoutView.PayoutItemRow;
import com.glm.payout.entity.Payout;
import com.glm.payout.entity.PayoutItem;
import com.glm.payout.repository.PayoutItemRepository;
import com.glm.payout.repository.PayoutRepository;
import com.glm.refund.entity.Refund;
import com.glm.refund.repository.RefundRepository;
import com.glm.user.entity.User;
import com.glm.user.repository.SellerBankAccountRepository;
import com.glm.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class PayoutService {

    private static final Logger log = LoggerFactory.getLogger(PayoutService.class);
    private static final Set<Refund.Status> OPEN_REFUND = Set.of(Refund.Status.REQUESTED, Refund.Status.APPROVED);

    private final OrderRepository          orderRepo;
    private final PayoutRepository         payoutRepo;
    private final PayoutItemRepository     payoutItemRepo;
    private final RefundRepository         refundRepo;
    private final SellerBankAccountRepository bankRepo;
    private final UserRepository           userRepo;
    private final NotificationRepository   notifRepo;
    private final AuditLogRepository       auditRepo;

    public PayoutService(OrderRepository orderRepo, PayoutRepository payoutRepo,
                          PayoutItemRepository payoutItemRepo, RefundRepository refundRepo,
                          SellerBankAccountRepository bankRepo, UserRepository userRepo,
                          NotificationRepository notifRepo, AuditLogRepository auditRepo) {
        this.orderRepo     = orderRepo;
        this.payoutRepo    = payoutRepo;
        this.payoutItemRepo = payoutItemRepo;
        this.refundRepo    = refundRepo;
        this.bankRepo      = bankRepo;
        this.userRepo      = userRepo;
        this.notifRepo     = notifRepo;
        this.auditRepo     = auditRepo;
    }

    /** Returns COMPLETED orders without a payout, grouped by seller (admin view). */
    @Transactional(readOnly = true)
    public List<EligibleSellerView> eligibleBySeller() {
        List<Order> eligible = orderRepo.findPayoutEligible().stream()
            .filter(o -> !hasOpenRefund(o)).toList();
        return groupBySeller(eligible);
    }

    /** Returns eligible orders and payout history for a specific seller. */
    @Transactional(readOnly = true)
    public Map<String, Object> sellerPayoutsView(User seller) {
        List<Order> eligible = orderRepo.findPayoutEligible().stream()
            .filter(o -> o.getSeller().getId().equals(seller.getId()))
            .filter(o -> !hasOpenRefund(o)).toList();
        List<PayoutView> history = payoutRepo.findBySellerIdOrderByCreatedAtDesc(seller.getId())
            .stream().map(this::toView).toList();
        List<EligibleOrderView> eligibleViews = eligible.stream()
            .map(o -> new EligibleOrderView(o.getId(), o.getOrderNo(), o.getSellerNet(), o.getCompletedAt()))
            .toList();
        BigDecimal totalNet = eligible.stream().map(Order::getSellerNet)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("eligibleOrders", eligibleViews, "totalNet", totalNet, "history", history);
    }

    /** Creates a payout batch for a seller. UNIQUE DB constraint absorbs concurrent races (E-PO-DUP). */
    @Transactional
    public PayoutView create(CreatePayoutRequest req, User admin) {
        User seller = userRepo.findById(req.sellerId()).orElseThrow(() ->
            new DomainException(ErrorCode.E_NOTFOUND, "Seller not found", 404));
        bankRepo.findByUserId(seller.getId()).filter(b -> b.isVerified()).orElseThrow(() ->
            new DomainException(ErrorCode.E_PO_BANK, "Seller bank account missing or unverified", 422));

        List<Order> orders = req.orderIds().stream()
            .map(id -> orderRepo.findById(id).orElseThrow(() ->
                new DomainException(ErrorCode.E_NOTFOUND, "Order not found: " + id, 404)))
            .toList();

        for (Order o : orders) {
            if (o.getStatus() != Order.Status.COMPLETED)
                throw new DomainException(ErrorCode.E_ORD_STATE, "Order " + o.getOrderNo() + " is not COMPLETED", 409);
            if (!o.getSeller().getId().equals(seller.getId()))
                throw new DomainException(ErrorCode.E_AUTH_OWN, "Order does not belong to this seller", 403);
            if (hasOpenRefund(o))
                throw new DomainException(ErrorCode.E_ORD_STATE, "Order " + o.getOrderNo() + " has an open refund", 409);
        }

        BigDecimal total = orders.stream().map(Order::getSellerNet).reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant now = Instant.now();
        Payout payout = new Payout();
        payout.setPayoutNo(generatePayoutNo());
        payout.setSeller(seller);
        payout.setAmount(total);
        payout.setStatus(Payout.Status.PENDING);
        payout.setAdmin(admin);
        payout.setCreatedAt(now);
        payout.setUpdatedAt(now);

        try {
            payoutRepo.save(payout);
            for (Order o : orders) {
                PayoutItem item = new PayoutItem();
                item.setPayout(payout);
                item.setOrder(o);
                item.setAmount(o.getSellerNet());
                payoutItemRepo.save(item);
            }
        } catch (DataIntegrityViolationException e) {
            throw new DomainException(ErrorCode.E_PO_DUP, "One or more orders already in another payout", 409);
        }

        audit(admin, "PAYOUT_CREATE", "payout", payout.getId());
        log.info("[PAYOUT] created payoutNo={} sellerId={} amount={}", payout.getPayoutNo(), seller.getId(), total);
        return toView(payout);
    }

    /** Marks a payout PAID with the bank transfer reference. Atomic with audit log. */
    @Transactional
    public void markPaid(Long payoutId, String bankRef, User admin) {
        Payout payout = payoutRepo.findById(payoutId).orElseThrow(() ->
            new DomainException(ErrorCode.E_NOTFOUND, "Payout not found: " + payoutId, 404));
        if (payout.getStatus() != Payout.Status.PENDING)
            throw new DomainException(ErrorCode.E_PAY_STATE, "Payout already finalized", 409);

        Instant now = Instant.now();
        payout.setStatus(Payout.Status.PAID);
        payout.setBankRef(bankRef);
        payout.setPaidAt(now);
        payout.setUpdatedAt(now);
        payoutRepo.save(payout);

        audit(admin, "PAYOUT_MARK_PAID", "payout", payoutId);
        Notification n = new Notification();
        n.setUser(payout.getSeller());
        n.setType("PAYOUT_PAID");
        n.setTitle("Payout received");
        n.setBody("Your payout of RM " + payout.getAmount() + " (" + payout.getPayoutNo() + ") has been transferred. Ref: " + bankRef);
        n.setCreatedAt(now);
        notifRepo.save(n);
        log.info("[PAYOUT] PAID payoutNo={} bankRef={}", payout.getPayoutNo(), bankRef);
    }

    @Transactional(readOnly = true)
    public List<PayoutView> listPending() {
        return payoutRepo.findByStatus(Payout.Status.PENDING).stream().map(this::toView).toList();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private boolean hasOpenRefund(Order o) {
        return refundRepo.findByOrderId(o.getId())
            .map(r -> OPEN_REFUND.contains(r.getStatus())).orElse(false);
    }

    private List<EligibleSellerView> groupBySeller(List<Order> orders) {
        Map<Long, List<Order>> bySeller = orders.stream()
            .collect(Collectors.groupingBy(o -> o.getSeller().getId(), LinkedHashMap::new, Collectors.toList()));
        return bySeller.entrySet().stream().map(e -> {
            List<Order> grp = e.getValue();
            User seller = grp.get(0).getSeller();
            var bank = bankRepo.findByUserId(seller.getId());
            BigDecimal net = grp.stream().map(Order::getSellerNet).reduce(BigDecimal.ZERO, BigDecimal::add);
            List<EligibleOrderView> items = grp.stream()
                .map(o -> new EligibleOrderView(o.getId(), o.getOrderNo(), o.getSellerNet(), o.getCompletedAt()))
                .toList();
            return new EligibleSellerView(seller.getId(), seller.getName(),
                bank.map(b -> b.getBankName()).orElse(null),
                bank.map(b -> "····" + b.getAccountNo().substring(Math.max(0, b.getAccountNo().length()-4))).orElse(null),
                bank.map(b -> b.isVerified()).orElse(false),
                grp.size(), net, items);
        }).toList();
    }

    private PayoutView toView(Payout p) {
        List<PayoutItemRow> items = payoutItemRepo.findByPayoutId(p.getId()).stream()
            .map(i -> new PayoutItemRow(i.getOrder().getOrderNo(), i.getAmount())).toList();
        return new PayoutView(p.getId(), p.getPayoutNo(), p.getSeller().getName(),
            p.getAmount(), p.getStatus().name(), p.getBankRef(), p.getPaidAt(), p.getCreatedAt(), items);
    }

    private String generatePayoutNo() {
        String yymm = LocalDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"))
            .format(DateTimeFormatter.ofPattern("yyMM"));
        return "PO-" + yymm + "-" + String.format("%06d", ThreadLocalRandom.current().nextInt(999999));
    }

    private void audit(User admin, String action, String entityType, Long entityId) {
        AuditLog al = new AuditLog();
        al.setUser(admin); al.setAction(action);
        al.setEntityType(entityType); al.setEntityId(entityId);
        al.setCreatedAt(Instant.now());
        auditRepo.save(al);
    }
}
