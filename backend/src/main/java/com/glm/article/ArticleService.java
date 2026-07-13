package com.glm.article;

import com.glm.article.dto.ArticleAdminView;
import com.glm.article.dto.ArticleDetail;
import com.glm.article.dto.ArticleRequest;
import com.glm.article.dto.ArticleSummary;
import com.glm.article.entity.Article;
import com.glm.article.entity.Article.Status;
import com.glm.article.repository.ArticleRepository;
import com.glm.catalog.SlugGenerator;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ArticleService {

    private final ArticleRepository articleRepo;
    private final SlugGenerator slugGen;

    public ArticleService(ArticleRepository articleRepo, SlugGenerator slugGen) {
        this.articleRepo = articleRepo;
        this.slugGen = slugGen;
    }

    @Transactional(readOnly = true)
    public List<ArticleSummary> listPublished() {
        return articleRepo.findByStatusOrderByPublishedAtDesc(Status.PUBLISHED).stream()
            .map(ArticleSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public ArticleDetail getPublished(String slug) {
        Article a = articleRepo.findBySlug(slug)
            .filter(x -> x.getStatus() == Status.PUBLISHED)
            .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Article not found", 404));
        return ArticleDetail.from(a);
    }

    @Transactional(readOnly = true)
    public List<ArticleAdminView> listAdmin() {
        return articleRepo.findAll().stream().map(ArticleAdminView::from).toList();
    }

    @Transactional(readOnly = true)
    public ArticleAdminView getAdmin(Long id) {
        return ArticleAdminView.from(require(id));
    }

    @Transactional
    public Article create(User author, ArticleRequest req) {
        Article a = new Article();
        a.setAuthor(author);
        Instant now = Instant.now();
        a.setCreatedAt(now);
        a.setUpdatedAt(now);
        applyRequest(a, req);
        a = articleRepo.save(a);
        a.setSlug(slugGen.generate(a.getTitle(), a.getId()));
        return articleRepo.save(a);
    }

    @Transactional
    public Article update(Long id, ArticleRequest req) {
        Article a = require(id);
        applyRequest(a, req);
        a.setUpdatedAt(Instant.now());
        return articleRepo.save(a);
    }

    @Transactional
    public void delete(Long id) {
        articleRepo.delete(require(id));
    }

    private void applyRequest(Article a, ArticleRequest req) {
        a.setTitle(req.title());
        a.setExcerpt(req.excerpt());
        a.setBodyMd(req.bodyMd());
        a.setCoverImage(req.coverImage());
        Status newStatus = Status.valueOf(req.status());
        if (newStatus == Status.PUBLISHED && a.getStatus() != Status.PUBLISHED) {
            a.setPublishedAt(Instant.now());
        }
        a.setStatus(newStatus);
    }

    private Article require(Long id) {
        return articleRepo.findById(id)
            .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Article not found: " + id, 404));
    }
}
