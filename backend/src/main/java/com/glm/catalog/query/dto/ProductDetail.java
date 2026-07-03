package com.glm.catalog.query.dto;

import com.glm.catalog.entity.Product;
import com.glm.catalog.entity.ProductImage;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductDetail(
        Long id,
        String title,
        String slug,
        String description,
        String itemCondition,
        BigDecimal price,
        int quantity,
        int availability,
        boolean allowMeetup,
        boolean allowShipping,
        BigDecimal shippingFee,
        String meetupLocation,
        String sustainabilityNote,
        String status,
        Long categoryId,
        String categoryName,
        List<ImageInfo> images,
        SellerInfo seller,
        Instant createdAt
) {
    public record ImageInfo(Long id, String path, boolean isPrimary, int sortOrder) {}

    public record SellerInfo(Long id, String name, Instant joinedAt, int activeListingCount) {}

    public static ProductDetail from(Product p, List<ProductImage> imgs, int availability, int activeListingCount) {
        List<ImageInfo> imageList = imgs.stream()
                .map(i -> new ImageInfo(i.getId(), i.getPath(), i.isPrimary(), i.getSortOrder()))
                .toList();
        SellerInfo sellerInfo = new SellerInfo(
                p.getSeller().getId(), p.getSeller().getName(),
                p.getSeller().getCreatedAt(), activeListingCount
        );
        return new ProductDetail(
                p.getId(), p.getTitle(), p.getSlug(), p.getDescription(),
                p.getItemCondition().name(), p.getPrice(), p.getQuantity(), availability,
                p.isAllowMeetup(), p.isAllowShipping(), p.getShippingFee(),
                p.getMeetupLocation(), p.getSustainabilityNote(), p.getStatus().name(),
                p.getCategory().getId(), p.getCategory().getName(),
                imageList, sellerInfo, p.getCreatedAt()
        );
    }
}
