package com.glm.notification;

import com.glm.common.security.GlmUserDetails;
import com.glm.notification.dto.NotificationView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationView> list(@AuthenticationPrincipal GlmUserDetails principal) {
        return notificationService.list(principal.getUser());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@AuthenticationPrincipal GlmUserDetails principal,
                                          @PathVariable Long id) {
        notificationService.markRead(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal GlmUserDetails principal) {
        notificationService.markAllRead(principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
