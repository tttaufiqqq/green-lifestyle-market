package com.glm.notification.entity;

import com.glm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "notifications")
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type", length = 40, nullable = false)
    private String type;

    @Column(name = "title", length = 120, nullable = false)
    private String title;

    @Column(name = "body", length = 255, nullable = false)
    private String body;

    @Column(name = "data", columnDefinition = "JSON")
    private String data;

    @Column(name = "read_at", columnDefinition = "TIMESTAMP")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant createdAt;
}
