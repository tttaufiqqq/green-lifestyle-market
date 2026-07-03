package com.glm.order.repository;

import com.glm.order.entity.Order;
import com.glm.order.entity.Order.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    List<Order> findByPaymentId(Long paymentId);
    List<Order> findByBuyerIdAndStatus(Long buyerId, Status status);
    List<Order> findBySellerIdAndStatus(Long sellerId, Status status);
    List<Order> findByBuyerIdAndStatusInOrderByCreatedAtDesc(Long buyerId, Collection<Status> statuses);
    List<Order> findBySellerIdAndStatusInOrderByCreatedAtDesc(Long sellerId, Collection<Status> statuses);
    List<Order> findByStatusInAndShippedAtBefore(Collection<Status> statuses, Instant cutoff);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findAllByStatusOrderByCreatedAtDesc(Status status);

    /** All COMPLETED orders not yet associated with a payout item, ordered by seller. */
    @Query("""
        SELECT o FROM Order o WHERE o.status = com.glm.order.entity.Order.Status.COMPLETED
        AND NOT EXISTS (SELECT pi FROM com.glm.payout.entity.PayoutItem pi WHERE pi.order = o)
        ORDER BY o.seller.id ASC, o.completedAt ASC
        """)
    List<Order> findPayoutEligible();

    /** Sum of order totals for the given statuses (used for escrow balance). */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status IN :statuses")
    BigDecimal sumTotalByStatusIn(@Param("statuses") Collection<Status> statuses);

    /** Sum of platform fees for COMPLETED orders since a given instant (used for MTD fees). */
    @Query("SELECT COALESCE(SUM(o.platformFee), 0) FROM Order o WHERE o.status = 'COMPLETED' AND o.completedAt >= :from")
    BigDecimal sumPlatformFeeSince(@Param("from") Instant from);

    /** Count of orders per status (for dashboard). */
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countGroupByStatus();
}
