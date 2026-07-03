package com.glm.order.lifecycle;

import com.glm.order.OrderTransitionService;
import com.glm.order.entity.Order;
import com.glm.order.repository.OrderRepository;
import com.glm.refund.entity.Refund;
import com.glm.refund.repository.RefundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/**
 * Daily auto-complete job: SHIPPED/READY_FOR_MEETUP orders older than
 * {@code app.order.auto-complete-days} (default 7) with no open refund → COMPLETED.
 * Runs at 03:00 MYT (UTC+8) = 19:00 UTC.
 */
@Component
public class AutoCompleteJob {

    private static final Logger log = LoggerFactory.getLogger(AutoCompleteJob.class);

    private static final List<Order.Status> AUTO_STATUSES = List.of(
        Order.Status.SHIPPED, Order.Status.READY_FOR_MEETUP);

    private static final Set<Refund.Status> OPEN_REFUND = Set.of(
        Refund.Status.REQUESTED, Refund.Status.APPROVED);

    @Value("${app.auto-complete-days:7}")
    private int autoCompleteDays;

    private final OrderRepository orderRepo;
    private final RefundRepository refundRepo;
    private final OrderTransitionService transitionService;

    public AutoCompleteJob(OrderRepository orderRepo, RefundRepository refundRepo,
                           OrderTransitionService transitionService) {
        this.orderRepo         = orderRepo;
        this.refundRepo        = refundRepo;
        this.transitionService = transitionService;
    }

    @Scheduled(cron = "0 0 19 * * *") // 19:00 UTC = 03:00 MYT (UTC+8)
    public void run() {
        Instant cutoff = Instant.now().minus(autoCompleteDays, ChronoUnit.DAYS);
        List<Order> candidates = orderRepo.findByStatusInAndShippedAtBefore(AUTO_STATUSES, cutoff);
        int completed = 0;

        for (Order order : candidates) {
            boolean hasOpenRefund = refundRepo.findByOrderId(order.getId())
                .map(r -> OPEN_REFUND.contains(r.getStatus()))
                .orElse(false);
            if (hasOpenRefund) {
                log.info("[AUTO-COMPLETE] skip orderNo={} open refund", order.getOrderNo());
                continue;
            }
            try {
                transitionService.autoComplete(order);
                completed++;
            } catch (Exception e) {
                log.error("[AUTO-COMPLETE] failed orderNo={} error={}", order.getOrderNo(), e.getMessage());
            }
        }
        log.info("[AUTO-COMPLETE] completed={}/{}", completed, candidates.size());
    }
}
