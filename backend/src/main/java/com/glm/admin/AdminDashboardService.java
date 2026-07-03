package com.glm.admin;

import com.glm.admin.dto.DashboardView;
import com.glm.catalog.entity.Product;
import com.glm.catalog.repository.ProductRepository;
import com.glm.order.entity.Order.Status;
import com.glm.order.repository.OrderRepository;
import com.glm.payment.entity.Payment;
import com.glm.payment.repository.PaymentRepository;
import com.glm.payout.entity.Payout;
import com.glm.payout.repository.PayoutRepository;
import com.glm.refund.entity.Refund;
import com.glm.refund.repository.RefundRepository;
import com.glm.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardService {

    private static final ZoneId MYT = ZoneId.of("Asia/Kuala_Lumpur");
    private static final List<Status> ESCROW_STATUSES = List.of(
        Status.PAID, Status.CONFIRMED, Status.SHIPPED, Status.READY_FOR_MEETUP, Status.REFUND_REQUESTED);

    private final UserRepository    userRepo;
    private final ProductRepository productRepo;
    private final OrderRepository   orderRepo;
    private final PaymentRepository paymentRepo;
    private final RefundRepository  refundRepo;
    private final PayoutRepository  payoutRepo;

    public AdminDashboardService(UserRepository userRepo, ProductRepository productRepo,
                                  OrderRepository orderRepo, PaymentRepository paymentRepo,
                                  RefundRepository refundRepo, PayoutRepository payoutRepo) {
        this.userRepo    = userRepo;
        this.productRepo = productRepo;
        this.orderRepo   = orderRepo;
        this.paymentRepo = paymentRepo;
        this.refundRepo  = refundRepo;
        this.payoutRepo  = payoutRepo;
    }

    @Transactional(readOnly = true)
    public DashboardView build() {
        long userCount      = userRepo.count();
        long activeListings = productRepo.countByStatus(Product.Status.ACTIVE);
        BigDecimal escrow   = orderRepo.sumTotalByStatusIn(ESCROW_STATUSES);
        BigDecimal feesMtd  = orderRepo.sumPlatformFeeSince(startOfCurrentMonth());

        Map<String, Long> byStatus = new HashMap<>();
        for (Object[] row : orderRepo.countGroupByStatus())
            byStatus.put(row[0].toString(), ((Number) row[1]).longValue());

        long reviewPayments = paymentRepo.countByStatus(Payment.Status.REVIEW);
        long pendingRefunds = refundRepo.findByStatus(Refund.Status.REQUESTED).size();
        long pendingPayouts = payoutRepo.findByStatus(Payout.Status.PENDING).size();

        return new DashboardView(userCount, activeListings,
            escrow == null ? BigDecimal.ZERO : escrow,
            feesMtd == null ? BigDecimal.ZERO : feesMtd,
            byStatus, reviewPayments, pendingRefunds, pendingPayouts);
    }

    private Instant startOfCurrentMonth() {
        ZonedDateTime now = ZonedDateTime.now(MYT);
        return now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant();
    }
}
