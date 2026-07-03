package com.glm.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UserStatusRequest(@NotBlank String status) {}
