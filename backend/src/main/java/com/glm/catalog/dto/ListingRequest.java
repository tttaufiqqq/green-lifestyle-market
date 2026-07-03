package com.glm.catalog.dto;

import com.glm.catalog.entity.Product.ItemCondition;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ListingRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank String description,
        @NotNull Long categoryId,
        @NotNull ItemCondition itemCondition,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @Min(1) int quantity,
        boolean allowMeetup,
        boolean allowShipping,
        BigDecimal shippingFee,
        @Size(max = 120) String meetupLocation,
        @Size(max = 255) String sustainabilityNote,
        String status
) {}
