package com.glm.catalog;

import com.glm.catalog.dto.ListingRequest;
import com.glm.catalog.entity.Product;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.entity.Order;
import com.glm.order.entity.StockReservation;
import com.glm.order.repository.StockReservationRepository;
import com.glm.user.entity.User;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class ListingValidator {

    private static final Set<Order.Status> TERMINAL = EnumSet.of(
            Order.Status.COMPLETED, Order.Status.CANCELLED,
            Order.Status.EXPIRED, Order.Status.REFUNDED);

    private final StockReservationRepository reservationRepo;
    private final EntityManager em;

    public ListingValidator(StockReservationRepository reservationRepo, EntityManager em) {
        this.reservationRepo = reservationRepo;
        this.em = em;
    }

    public void requireEmailVerified(User user) {
        if (user.getEmailVerifiedAt() == null)
            throw new DomainException(ErrorCode.E_AUTH_VERIFY, "Verify your email first", 422);
    }

    public void validateFulfilment(ListingRequest req) {
        if (!req.allowMeetup() && !req.allowShipping())
            throw new DomainException(ErrorCode.E_LIST_FULFIL,
                    "Listing needs at least one fulfilment option", 422);
        if (req.allowShipping() && req.shippingFee() == null)
            throw new DomainException(ErrorCode.E_LIST_FULFIL,
                    "Shipping fee is required when shipping is allowed", 422);
        if (req.allowMeetup() && (req.meetupLocation() == null || req.meetupLocation().isBlank()))
            throw new DomainException(ErrorCode.E_LIST_FULFIL,
                    "Meetup location is required when meetup is allowed", 422);
    }

    public void validateQtyNotBelowHeld(Product product, int newQty) {
        int held = reservationRepo
                .findByProductIdAndStatus(product.getId(), StockReservation.Status.HELD)
                .stream().mapToInt(StockReservation::getQuantity).sum();
        if (newQty < held)
            throw new DomainException(ErrorCode.E_LIST_QTY_HELD,
                    "Quantity is below held reservations", 409);
    }

    public void requireNoOpenOrders(Product product) {
        Long count = em.createQuery(
                        "SELECT COUNT(oi) FROM OrderItem oi " +
                        "WHERE oi.product.id = :pid AND oi.order.status NOT IN :terminals",
                        Long.class)
                .setParameter("pid", product.getId())
                .setParameter("terminals", TERMINAL)
                .getSingleResult();
        if (count > 0)
            throw new DomainException(ErrorCode.E_LIST_OPEN_ORDERS,
                    "Cannot delete: open orders exist", 409);
    }
}
