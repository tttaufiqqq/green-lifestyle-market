package com.glm.user.dto;

import com.glm.user.entity.User;

public record ProfileResponse(
    Long id,
    String name,
    String email,
    String phone,
    String affiliation,
    boolean emailVerified
) {
    public static ProfileResponse from(User u) {
        return new ProfileResponse(
            u.getId(), u.getName(), u.getEmail(), u.getPhone(),
            u.getAffiliation().name(), u.getEmailVerifiedAt() != null
        );
    }
}
