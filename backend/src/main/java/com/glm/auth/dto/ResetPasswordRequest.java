package com.glm.auth.dto;

import jakarta.validation.constraints.*;

public record ResetPasswordRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 8, max = 100) String newPassword
) {}
