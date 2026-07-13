package com.glm.order.lifecycle;

import com.glm.notification.entity.Notification;
import com.glm.notification.repository.NotificationRepository;
import com.glm.order.entity.Order;
import com.glm.user.entity.User;
import com.glm.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Writes notification rows for order lifecycle events in the same transaction as the state change
 * (outbox-lite pattern). Push delivery is wired in spec 10.
 */
@Component
public class OrderNotifier {

    private static final Logger log = LoggerFactory.getLogger(OrderNotifier.class);
    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;

    public OrderNotifier(NotificationRepository notificationRepo, UserRepository userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    public void onConfirmed(Order order) {
        write(order.getBuyer(), "ORDER_CONFIRMED", "Order confirmed",
            "Seller confirmed order " + order.getOrderNo() + ". Awaiting fulfilment.",
            order.getOrderNo());
    }

    public void onCancelled(Order order, String reason) {
        String msg = "Order " + order.getOrderNo() + " has been cancelled."
            + (reason != null && !reason.isBlank() ? " Reason: " + reason : "");
        write(order.getBuyer(), "ORDER_CANCELLED", "Order cancelled", msg, order.getOrderNo());
        write(order.getSeller(), "ORDER_CANCELLED", "Order cancelled",
            "Order " + order.getOrderNo() + " was cancelled.", order.getOrderNo());
    }

    public void onShipped(Order order) {
        boolean isShipping = order.getFulfilmentMethod() == Order.FulfilmentMethod.SHIPPING;
        String type  = isShipping ? "ORDER_SHIPPED" : "ORDER_MEETUP";
        String title = isShipping ? "Order shipped" : "Ready for meetup";
        String body  = isShipping
            ? "Your order was shipped via " + order.getCourier() + " (tracking: " + order.getTrackingNo() + ")."
            : "Your order is ready for meetup. Note: " + order.getMeetupNote();
        write(order.getBuyer(), type, title, body, order.getOrderNo());
    }

    public void onCompleted(Order order) {
        write(order.getBuyer(), "ORDER_AUTOCOMPLETED", "Order completed",
            "Order " + order.getOrderNo() + " has been completed. Thank you!", order.getOrderNo());
        write(order.getSeller(), "ORDER_AUTOCOMPLETED", "Order completed",
            "Order " + order.getOrderNo() + " is now completed.", order.getOrderNo());
    }

    /** Docs/notifications.md: refund REQUESTED notifies admin + seller. */
    public void onRefundRequested(Order order) {
        write(order.getSeller(), "REFUND_REQUESTED", "Refund requested",
            "Buyer requested a refund for order " + order.getOrderNo() + ". Pending admin review.",
            order.getOrderNo());
        userRepo.findAll().stream()
            .filter(u -> u.getRole() == User.Role.ADMIN)
            .forEach(admin -> write(admin, "REFUND_REQUESTED", "Refund requested",
                "Buyer requested a refund for order " + order.getOrderNo() + ". Review needed.",
                order.getOrderNo()));
    }

    private void write(User user, String type, String title, String body, String ref) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setData("{\"ref\":\"" + ref + "\"}");
        n.setCreatedAt(Instant.now());
        notificationRepo.save(n);
        log.info("[NOTIFY] type={} userId={} ref={}", type, user.getId(), ref);
    }
}
