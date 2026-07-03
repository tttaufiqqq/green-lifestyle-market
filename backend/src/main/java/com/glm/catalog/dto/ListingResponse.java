package com.glm.catalog.dto;

import com.glm.catalog.entity.Product;
import com.glm.catalog.entity.ProductImage;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ListingResponse(
        Long id,
        String title,
        String slug,
        String description,
        String itemCondition,
        BigDecimal price,
        int quantity,
        boolean allowMeetup,
        boolean allowShipping,
        BigDecimal shippingFee,
        String meetupLocation,
        String sustainabilityNote,
        String status,
        Long categoryId,
        String categoryName,
        Long sellerId,
        String sellerName,
        List<ImageResponse> images,
        Instant createdAt,
        Instant updatedAt
) {
    public record ImageResponse(Long id, String path, boolean isPrimary, int sortOrder) {}

    public static ListingResponse from(Product p, List<ProductImage> imgs) {
        List<ImageResponse> imageList = imgs.stream()
                .map(i -> new ImageResponse(i.getId(), i.getPath(), i.isPrimary(), i.getSortOrder()))
                .toList();
        return new ListingResponse(
                p.getId(), p.getTitle(), p.getSlug(), p.getDescription(),
                p.getItemCondition().name(), p.getPrice(), p.getQuantity(),
                p.isAllowMeetup(), p.isAllowShipping(), p.getShippingFee(),
                p.getMeetupLocation(), p.getSustainabilityNote(), p.getStatus().name(),
                p.getCategory().getId(), p.getCategory().getName(),
                p.getSeller().getId(), p.getSeller().getName(),
                imageList, p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
