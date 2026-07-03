package com.glm.notification.entity;

import com.glm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "push_subscriptions")
public class PushSubscription {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "endpoint", length = 500, nullable = false, unique = true)
    private String endpoint;

    @Column(name = "p256dh", length = 120, nullable = false)
    private String p256dh;

    @Column(name = "auth", length = 40, nullable = false)
    private String auth;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant createdAt;
}
