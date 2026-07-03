package com.glm.cart.dto;

import jakarta.validation.constraints.Min;

public record UpdateQuantityRequest(@Min(1) int quantity) {}
