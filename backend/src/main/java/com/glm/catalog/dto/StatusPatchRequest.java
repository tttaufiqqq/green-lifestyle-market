package com.glm.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusPatchRequest(@NotBlank String status) {}
