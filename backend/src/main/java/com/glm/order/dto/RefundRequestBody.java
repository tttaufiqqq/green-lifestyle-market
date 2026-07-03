package com.glm.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefundRequestBody(
    @NotBlank @Size(max = 500) String reason
) {}
