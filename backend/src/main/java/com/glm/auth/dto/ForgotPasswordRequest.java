package com.glm.auth.dto;

import jakarta.validation.constraints.*;

public record ForgotPasswordRequest(@NotBlank @Email String email) {}
