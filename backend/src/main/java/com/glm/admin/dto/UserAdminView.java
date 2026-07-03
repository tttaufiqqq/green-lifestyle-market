package com.glm.admin.dto;

import java.time.Instant;

public record UserAdminView(
    Long id,
    String name,
    String email,
    String phone,
    String role,
    String status,
    boolean emailVerified,
    Instant createdAt
) {}
