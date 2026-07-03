package com.glm.article.repository;

import com.glm.article.entity.Article;
import com.glm.article.entity.Article.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findBySlug(String slug);
    List<Article> findByStatusOrderByPublishedAtDesc(Status status);
}
