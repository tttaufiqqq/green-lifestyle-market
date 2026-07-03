package com.glm.refund.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProcessRefundRequest(
    @NotBlank String bankRef,
    @Size(max = 255) String adminNote
) {}
