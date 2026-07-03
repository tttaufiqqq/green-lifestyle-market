package com.glm.auth.dto;

import com.glm.user.entity.User;

public record MeResponse(
    Long id,
    String name,
    String email,
    String role,
    String affiliation,
    boolean emailVerified,
    int cartCount
) {
    public static MeResponse from(User u, int cartCount) {
        return new MeResponse(
            u.getId(), u.getName(), u.getEmail(),
            u.getRole().name(), u.getAffiliation().name(),
            u.getEmailVerifiedAt() != null,
            cartCount
        );
    }
}
