package com.glm.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glm.notification.dto.NotificationView;
import com.glm.notification.entity.Notification;
import com.glm.notification.entity.PushSubscription;
import com.glm.notification.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.List;
import java.util.Map;

/**
 * Fans out a persisted Notification row (already committed) to: (a) a live STOMP session,
 * (b) all of the user's Web Push subscriptions. Delivery failures are logged and never
 * propagate — the domain transaction that wrote the row has already committed.
 */
@Component
public class NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final PushSubscriptionRepository pushSubscriptionRepo;
    private final ObjectMapper objectMapper;
    private final PushService pushService;

    public NotificationPublisher(SimpMessagingTemplate messagingTemplate,
                                  PushSubscriptionRepository pushSubscriptionRepo,
                                  ObjectMapper objectMapper,
                                  @Value("${app.vapid.public-key}") String vapidPublicKey,
                                  @Value("${app.vapid.private-key}") String vapidPrivateKey,
                                  @Value("${app.vapid.subject}") String vapidSubject) throws Exception {
        this.messagingTemplate = messagingTemplate;
        this.pushSubscriptionRepo = pushSubscriptionRepo;
        this.objectMapper = objectMapper;
        Security.addProvider(new BouncyCastleProvider());
        this.pushService = PushService.builder()
            .withVapidPublicKey(vapidPublicKey)
            .withVapidPrivateKey(vapidPrivateKey)
            .withVapidSubject(vapidSubject)
            .build();
    }

    public void publish(Notification notification) {
        sendWs(notification);
        sendPush(notification);
    }

    private void sendWs(Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                notification.getUser().getEmail(), "/queue/notifications", NotificationView.from(notification));
        } catch (Exception e) {
            log.warn("[WS] delivery failed notificationId={} err={}", notification.getId(), e.getMessage());
        }
    }

    private void sendPush(Notification notification) {
        List<PushSubscription> subs = pushSubscriptionRepo.findByUserId(notification.getUser().getId());
        for (PushSubscription sub : subs) {
            sendPushTo(sub, notification);
        }
    }

    private void sendPushTo(PushSubscription sub, Notification notification) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                "type", notification.getType(),
                "title", notification.getTitle(),
                "body", notification.getBody(),
                "data", notification.getData() == null ? "" : notification.getData()));

            var pushNotification = nl.martijndwars.webpush.Notification.builder()
                .endpoint(sub.getEndpoint())
                .userPublicKey(sub.getP256dh())
                .userAuth(sub.getAuth())
                .payload(payload.getBytes(StandardCharsets.UTF_8))
                .build();

            var response = pushService.send(pushNotification);
            int status = response.statusCode();
            if (status == 404 || status == 410) {
                pushSubscriptionRepo.delete(sub);
                log.info("[PUSH] pruned dead subscription id={} status={}", sub.getId(), status);
            }
        } catch (Exception e) {
            log.warn("[PUSH] delivery failed subscriptionId={} err={}", sub.getId(), e.getMessage());
        }
    }
}
