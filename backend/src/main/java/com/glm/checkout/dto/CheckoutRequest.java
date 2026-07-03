package com.glm.checkout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CheckoutRequest(
    @NotEmpty @Valid List<FulfilmentChoice> fulfilments
) {}
