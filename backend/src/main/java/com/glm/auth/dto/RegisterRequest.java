package com.glm.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Email @Size(max = 190) String email,
    @NotBlank @Size(min = 8, max = 100) String password,
    @Size(max = 15) String phone,
    String affiliation
) {}
