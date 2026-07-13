package com.glm.article.dto;

import com.glm.article.entity.Article;
import java.time.Instant;

public record ArticleDetail(
    Long id, String title, String slug, String excerpt, String bodyMd, String coverImage,
    String authorName, Instant publishedAt
) {
    public static ArticleDetail from(Article a) {
        return new ArticleDetail(a.getId(), a.getTitle(), a.getSlug(), a.getExcerpt(),
            a.getBodyMd(), a.getCoverImage(), a.getAuthor().getName(), a.getPublishedAt());
    }
}
