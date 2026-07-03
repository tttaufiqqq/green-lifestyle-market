package com.glm.catalog;

import com.glm.catalog.dto.CategoryRequest;
import com.glm.catalog.dto.CategoryResponse;
import com.glm.catalog.entity.Category;
import com.glm.catalog.repository.CategoryRepository;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepo;

    public CategoryService(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getPublicTree() {
        List<Category> all = categoryRepo.findByIsActiveTrueOrderBySortOrderAsc();
        return buildTree(all, null);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAdminList() {
        return categoryRepo.findAll().stream()
                .map(c -> CategoryResponse.from(c, List.of()))
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        Category cat = new Category();
        applyRequest(cat, req);
        return CategoryResponse.from(categoryRepo.save(cat), List.of());
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req) {
        Category cat = findOrThrow(id);
        applyRequest(cat, req);
        return CategoryResponse.from(categoryRepo.save(cat), List.of());
    }

    @Transactional
    public void delete(Long id) {
        Category cat = findOrThrow(id);
        cat.setActive(false);
        categoryRepo.save(cat);
    }

    private void applyRequest(Category cat, CategoryRequest req) {
        cat.setName(req.name());
        cat.setSortOrder(req.sortOrder());
        cat.setSlug(toSlug(req.name()));
        cat.setActive(true);
        if (req.parentId() != null) {
            cat.setParent(categoryRepo.findById(req.parentId())
                    .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND,
                            "Parent category not found", 404)));
        } else {
            cat.setParent(null);
        }
    }

    private List<CategoryResponse> buildTree(List<Category> all, Long parentId) {
        return all.stream()
                .filter(c -> {
                    Long pid = c.getParent() != null ? c.getParent().getId() : null;
                    return parentId == null ? pid == null : parentId.equals(pid);
                })
                .map(c -> CategoryResponse.from(c, buildTree(all, c.getId())))
                .toList();
    }

    private Category findOrThrow(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND,
                        "Category not found", 404));
    }

    private String toSlug(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}
