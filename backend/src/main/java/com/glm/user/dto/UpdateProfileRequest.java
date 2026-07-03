package com.glm.user.dto;

import jakarta.validation.constraints.*;

public record UpdateProfileRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 15) String phone,
    String affiliation
) {}
