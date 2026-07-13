package com.glm.notification;

import com.glm.common.security.GlmUserDetails;
import com.glm.notification.dto.PushSubscriptionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/push-subscriptions")
public class PushSubscriptionController {

    private final PushSubscriptionService pushSubscriptionService;

    public PushSubscriptionController(PushSubscriptionService pushSubscriptionService) {
        this.pushSubscriptionService = pushSubscriptionService;
    }

    @PostMapping
    public ResponseEntity<Void> register(@AuthenticationPrincipal GlmUserDetails principal,
                                          @Valid @RequestBody PushSubscriptionRequest req) {
        pushSubscriptionService.register(principal.getUser(), req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(@AuthenticationPrincipal GlmUserDetails principal,
                                        @RequestParam String endpoint) {
        pushSubscriptionService.remove(principal.getUser(), endpoint);
        return ResponseEntity.noContent().build();
    }
}
