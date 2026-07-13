package com.glm.notification;

import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.notification.dto.NotificationView;
import com.glm.notification.entity.Notification;
import com.glm.notification.repository.NotificationRepository;
import com.glm.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepo;

    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @Transactional(readOnly = true)
    public List<NotificationView> list(User user) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(NotificationView::from)
            .toList();
    }

    @Transactional
    public void markRead(User user, Long id) {
        Notification n = notificationRepo.findById(id)
            .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Notification not found", 404));
        if (!n.getUser().getId().equals(user.getId())) {
            throw new DomainException(ErrorCode.E_AUTH_OWN, "Not your notification", 403);
        }
        if (n.getReadAt() == null) {
            n.setReadAt(Instant.now());
            notificationRepo.save(n);
        }
    }

    @Transactional
    public void markAllRead(User user) {
        notificationRepo.markAllReadForUser(user.getId());
    }
}
