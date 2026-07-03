package com.glm.catalog.query.dto;

import com.glm.catalog.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductSummary(
        Long id,
        String title,
        String slug,
        BigDecimal price,
        String itemCondition,
        int quantity,
        boolean allowMeetup,
        boolean allowShipping,
        String primaryImagePath,
        Long categoryId,
        String categoryName,
        Instant createdAt
) {
    public static ProductSummary from(Product p, String primaryImagePath) {
        return new ProductSummary(
                p.getId(), p.getTitle(), p.getSlug(), p.getPrice(),
                p.getItemCondition().name(), p.getQuantity(),
                p.isAllowMeetup(), p.isAllowShipping(),
                primaryImagePath,
                p.getCategory().getId(), p.getCategory().getName(),
                p.getCreatedAt()
        );
    }
}
