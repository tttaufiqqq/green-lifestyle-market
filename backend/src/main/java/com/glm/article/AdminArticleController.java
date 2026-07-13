package com.glm.article;

import com.glm.article.dto.ArticleAdminView;
import com.glm.article.dto.ArticleRequest;
import com.glm.article.entity.Article;
import com.glm.common.audit.AuditLog;
import com.glm.common.audit.AuditLogRepository;
import com.glm.common.security.GlmUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/articles")
public class AdminArticleController {

    private final ArticleService articleService;
    private final AuditLogRepository auditRepo;

    public AdminArticleController(ArticleService articleService, AuditLogRepository auditRepo) {
        this.articleService = articleService;
        this.auditRepo = auditRepo;
    }

    @GetMapping
    public List<ArticleAdminView> list() {
        return articleService.listAdmin();
    }

    @GetMapping("/{id}")
    public ArticleAdminView get(@PathVariable Long id) {
        return articleService.getAdmin(id);
    }

    @PostMapping
    public ResponseEntity<ArticleAdminView> create(@Valid @RequestBody ArticleRequest req,
                                                    @AuthenticationPrincipal GlmUserDetails principal) {
        Article a = articleService.create(principal.getUser(), req);
        audit(principal.getUser(), "ARTICLE_CREATED", a.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ArticleAdminView.from(a));
    }

    @PutMapping("/{id}")
    public ArticleAdminView update(@PathVariable Long id, @Valid @RequestBody ArticleRequest req,
                                    @AuthenticationPrincipal GlmUserDetails principal) {
        Article a = articleService.update(id, req);
        audit(principal.getUser(), "ARTICLE_UPDATED", id);
        return ArticleAdminView.from(a);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal GlmUserDetails principal) {
        articleService.delete(id);
        audit(principal.getUser(), "ARTICLE_DELETED", id);
        return ResponseEntity.noContent().build();
    }

    private void audit(com.glm.user.entity.User admin, String action, Long entityId) {
        AuditLog al = new AuditLog();
        al.setUser(admin);
        al.setAction(action);
        al.setEntityType("article");
        al.setEntityId(entityId);
        al.setCreatedAt(Instant.now());
        auditRepo.save(al);
    }
}
