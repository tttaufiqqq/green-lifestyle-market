package com.glm.article.entity;

import com.glm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "articles")
public class Article {

    public enum Status { DRAFT, PUBLISHED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "slug", length = 170, nullable = false, unique = true)
    private String slug;

    @Column(name = "excerpt", length = 300, nullable = false)
    private String excerpt;

    @Lob @Column(name = "body_md", nullable = false)
    private String bodyMd;

    @Column(name = "cover_image", length = 255)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private Status status = Status.DRAFT;

    @Column(name = "published_at", columnDefinition = "TIMESTAMP")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant updatedAt;
}
