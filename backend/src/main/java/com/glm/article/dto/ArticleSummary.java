package com.glm.article.dto;

import com.glm.article.entity.Article;
import java.time.Instant;

public record ArticleSummary(
    Long id, String title, String slug, String excerpt, String coverImage,
    String authorName, Instant publishedAt
) {
    public static ArticleSummary from(Article a) {
        return new ArticleSummary(a.getId(), a.getTitle(), a.getSlug(), a.getExcerpt(),
            a.getCoverImage(), a.getAuthor().getName(), a.getPublishedAt());
    }
}
