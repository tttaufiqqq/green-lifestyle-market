package com.glm.notification.dto;

import com.glm.notification.entity.Notification;
import java.time.Instant;

public record NotificationView(
    Long id,
    String type,
    String title,
    String body,
    String data,
    Instant readAt,
    Instant createdAt
) {
    public static NotificationView from(Notification n) {
        return new NotificationView(n.getId(), n.getType(), n.getTitle(), n.getBody(),
            n.getData(), n.getReadAt(), n.getCreatedAt());
    }
}
