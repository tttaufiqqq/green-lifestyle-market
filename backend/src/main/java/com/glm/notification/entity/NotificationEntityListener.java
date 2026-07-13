package com.glm.notification.entity;

import com.glm.notification.NotificationPublisher;
import jakarta.persistence.PostPersist;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Bridges JPA's @PostPersist to Spring after-commit delivery via NotificationContext.
 * This lets every existing notificationRepo.save(...) call site (OrderNotifier,
 * PaymentSettleService, ...) get delivery for free — no changes to those callers.
 */
public class NotificationEntityListener {

    @PostPersist
    public void onPersist(Notification notification) {
        NotificationPublisher publisher = NotificationContext.publisher();
        if (publisher == null) return;

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.publish(notification);
                }
            });
        } else {
            publisher.publish(notification);
        }
    }
}
