package com.glm.checkout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Per-seller fulfilment selection included in both preview and checkout requests. */
public record FulfilmentChoice(
    @NotNull  Long   sellerId,
    @NotBlank String method,       // "MEETUP" or "SHIPPING"
    String shipName,
    String shipPhone,
    String shipAddress1,
    String shipAddress2,
    String shipPostcode,
    String shipCity,
    String shipState
) {}
