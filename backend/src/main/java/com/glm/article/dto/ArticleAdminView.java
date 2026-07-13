package com.glm.article.dto;

import com.glm.article.entity.Article;
import java.time.Instant;

public record ArticleAdminView(
    Long id, String title, String slug, String excerpt, String bodyMd, String coverImage,
    String status, String authorName, Instant publishedAt, Instant createdAt, Instant updatedAt
) {
    public static ArticleAdminView from(Article a) {
        return new ArticleAdminView(a.getId(), a.getTitle(), a.getSlug(), a.getExcerpt(),
            a.getBodyMd(), a.getCoverImage(), a.getStatus().name(), a.getAuthor().getName(),
            a.getPublishedAt(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
