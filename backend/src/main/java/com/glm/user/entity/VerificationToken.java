package com.glm.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "verification_tokens")
public class VerificationToken {

    public enum Purpose { EMAIL_VERIFY, PASSWORD_RESET }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 20, nullable = false)
    private Purpose purpose;

    @Column(name = "expires_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant expiresAt;

    @Column(name = "used_at", columnDefinition = "TIMESTAMP")
    private Instant usedAt;
}
