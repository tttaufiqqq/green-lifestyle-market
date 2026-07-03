package com.glm.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "webhook_events")
public class WebhookEvent {

    public enum Source { CALLBACK, RETURN, QUERY }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 10, nullable = false)
    private Source source;

    @Column(name = "idempotency_key", length = 80, nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "raw_payload", nullable = false, columnDefinition = "JSON")
    private String rawPayload;

    @Column(name = "processed", nullable = false)
    private boolean processed;

    @Column(name = "processed_at", columnDefinition = "TIMESTAMP")
    private Instant processedAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant createdAt;
}
