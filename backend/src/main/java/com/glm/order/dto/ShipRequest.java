package com.glm.order.dto;

import jakarta.validation.constraints.NotBlank;

public record ShipRequest(
    @NotBlank String courier,
    @NotBlank String trackingNo
) {}
