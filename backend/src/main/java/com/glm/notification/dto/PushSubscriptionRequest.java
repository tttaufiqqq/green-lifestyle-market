package com.glm.notification.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record PushSubscriptionRequest(
    @NotBlank String endpoint,
    @Valid Keys keys
) {
    public record Keys(@NotBlank String p256dh, @NotBlank String auth) {}
}
