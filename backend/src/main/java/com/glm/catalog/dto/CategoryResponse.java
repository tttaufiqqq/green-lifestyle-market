package com.glm.catalog.dto;

import com.glm.catalog.entity.Category;
import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        Long parentId,
        int sortOrder,
        boolean isActive,
        List<CategoryResponse> children
) {
    public static CategoryResponse from(Category c, List<CategoryResponse> children) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getSlug(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getSortOrder(),
                c.isActive(),
                children
        );
    }
}
