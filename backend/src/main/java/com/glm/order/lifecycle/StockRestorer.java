package com.glm.order.lifecycle;

import com.glm.catalog.entity.Product;
import com.glm.catalog.repository.ProductRepository;
import com.glm.order.entity.Order;
import com.glm.order.entity.StockReservation;
import com.glm.order.repository.StockReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Restores product stock on pre-handover cancellations.
 * - CONSUMED reservations (from PAID orders): restore qty; reactivate SOLD_OUT if qty > 0.
 * - HELD reservations (from PENDING_PAYMENT orders): release only, no qty change.
 * Must be called inside an active @Transactional context.
 */
@Component
public class StockRestorer {

    private static final Logger log = LoggerFactory.getLogger(StockRestorer.class);

    private final StockReservationRepository reservationRepo;
    private final ProductRepository productRepo;

    public StockRestorer(StockReservationRepository reservationRepo, ProductRepository productRepo) {
        this.reservationRepo = reservationRepo;
        this.productRepo = productRepo;
    }

    public void restore(Order order) {
        List<StockReservation> reservations = reservationRepo.findByOrderId(order.getId());
        for (StockReservation r : reservations) {
            if (r.getStatus() == StockReservation.Status.CONSUMED) {
                Product p = r.getProduct();
                int restored = p.getQuantity() + r.getQuantity();
                p.setQuantity(restored);
                if (p.getStatus() == Product.Status.SOLD_OUT && restored > 0) {
                    p.setStatus(Product.Status.ACTIVE);
                    log.info("[RESTORE] product {} reactivated (qty={})", p.getId(), restored);
                }
                productRepo.save(p);
                log.info("[RESTORE] orderId={} productId={} qty+{}", order.getId(), p.getId(), r.getQuantity());
            } else if (r.getStatus() == StockReservation.Status.HELD) {
                r.setStatus(StockReservation.Status.RELEASED);
                reservationRepo.save(r);
                log.info("[RESTORE] orderId={} released HELD reservation id={}", order.getId(), r.getId());
            }
        }
    }
}
