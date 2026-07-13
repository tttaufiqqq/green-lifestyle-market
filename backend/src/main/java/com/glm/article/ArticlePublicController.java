package com.glm.article;

import com.glm.article.dto.ArticleDetail;
import com.glm.article.dto.ArticleSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticlePublicController {

    private final ArticleService articleService;

    public ArticlePublicController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<ArticleSummary> list() {
        return articleService.listPublished();
    }

    @GetMapping("/{slug}")
    public ArticleDetail get(@PathVariable String slug) {
        return articleService.getPublished(slug);
    }
}
