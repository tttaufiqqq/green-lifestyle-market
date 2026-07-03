package com.glm.payout.dto;

import jakarta.validation.constraints.NotBlank;

public record MarkPaidRequest(@NotBlank String bankRef) {}
