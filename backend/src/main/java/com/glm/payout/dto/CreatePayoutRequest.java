package com.glm.payout.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreatePayoutRequest(
    @NotNull Long sellerId,
    @NotEmpty List<Long> orderIds
) {}
