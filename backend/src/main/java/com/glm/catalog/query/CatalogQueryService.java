package com.glm.catalog.query;

import com.glm.catalog.entity.Product;
import com.glm.catalog.entity.Product.Status;
import com.glm.catalog.entity.ProductImage;
import com.glm.catalog.query.dto.ProductDetail;
import com.glm.catalog.query.dto.ProductSummary;
import com.glm.catalog.query.dto.SearchParams;
import com.glm.catalog.repository.ProductImageRepository;
import com.glm.catalog.repository.ProductRepository;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.entity.StockReservation;
import com.glm.order.repository.StockReservationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CatalogQueryService {

    private final EntityManager em;
    private final ProductRepository productRepo;
    private final ProductImageRepository imageRepo;
    private final StockReservationRepository reservationRepo;

    public CatalogQueryService(EntityManager em, ProductRepository productRepo,
                               ProductImageRepository imageRepo,
                               StockReservationRepository reservationRepo) {
        this.em = em;
        this.productRepo = productRepo;
        this.imageRepo = imageRepo;
        this.reservationRepo = reservationRepo;
    }

    @Transactional(readOnly = true)
    public Page<ProductSummary> search(SearchParams params) {
        Set<Long> textIds = resolveTextIds(params.q());
        if (textIds != null && textIds.isEmpty())
            return Page.empty(PageRequest.of(params.page(), params.size()));

        String where = buildWhere(params, textIds);
        long total = count(where, params, textIds);
        if (total == 0) return Page.empty(PageRequest.of(params.page(), params.size()));

        List<Product> rows = fetchPage(where, params, textIds);
        Map<Long, String> imgs = batchPrimaryImages(rows.stream().map(Product::getId).toList());
        return new PageImpl<>(
                rows.stream().map(p -> ProductSummary.from(p, imgs.get(p.getId()))).toList(),
                PageRequest.of(params.page(), params.size()), total);
    }

    @Transactional(readOnly = true)
    public ProductDetail getProductBySlug(String slug) {
        Product p = productRepo.findBySlug(slug)
                .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Product not found", 404));
        Status s = p.getStatus();
        if (s == Status.DRAFT || s == Status.SUSPENDED || s == Status.DELETED)
            throw new DomainException(ErrorCode.E_NOTFOUND, "Product not found", 404);

        List<ProductImage> images = imageRepo.findByProductIdOrderBySortOrderAsc(p.getId());
        int held = reservationRepo.findByProductIdAndStatus(p.getId(), StockReservation.Status.HELD)
                .stream().mapToInt(StockReservation::getQuantity).sum();
        int availability = Math.max(0, p.getQuantity() - held);

        long activeCount = em.createQuery(
                        "SELECT COUNT(pr) FROM Product pr WHERE pr.seller.id = :sid AND pr.status = :active",
                        Long.class)
                .setParameter("sid", p.getSeller().getId())
                .setParameter("active", Status.ACTIVE)
                .getSingleResult();

        return ProductDetail.from(p, images, availability, (int) activeCount);
    }

    @SuppressWarnings("unchecked")
    private Set<Long> resolveTextIds(String q) {
        if (q == null || q.isBlank()) return null;
        List<Number> ids = em.createNativeQuery(
                "SELECT id FROM products WHERE CONTAINS(title, :q, 1) > 0 AND status = 'ACTIVE' AND quantity > 0")
                .setParameter("q", q).getResultList();
        return ids.stream().map(Number::longValue).collect(Collectors.toSet());
    }

    private String buildWhere(SearchParams params, Set<Long> textIds) {
        StringBuilder sb = new StringBuilder("prod.status = :active AND prod.quantity > 0");
        if (textIds != null)            sb.append(" AND prod.id IN :ids");
        if (params.categoryId() != null) sb.append(" AND prod.category.id = :categoryId");
        if (params.condition() != null)  sb.append(" AND prod.itemCondition = :condition");
        if (params.minPrice() != null)   sb.append(" AND prod.price >= :minPrice");
        if (params.maxPrice() != null)   sb.append(" AND prod.price <= :maxPrice");
        if ("MEETUP".equals(params.fulfilment()))   sb.append(" AND prod.allowMeetup = true");
        if ("SHIPPING".equals(params.fulfilment())) sb.append(" AND prod.allowShipping = true");
        return sb.toString();
    }

    private void applyParams(Query q, SearchParams params, Set<Long> textIds) {
        q.setParameter("active", Status.ACTIVE);
        if (textIds != null)            q.setParameter("ids", textIds);
        if (params.categoryId() != null) q.setParameter("categoryId", params.categoryId());
        if (params.condition() != null)  q.setParameter("condition", Product.ItemCondition.valueOf(params.condition()));
        if (params.minPrice() != null)   q.setParameter("minPrice", params.minPrice());
        if (params.maxPrice() != null)   q.setParameter("maxPrice", params.maxPrice());
    }

    private long count(String where, SearchParams params, Set<Long> textIds) {
        Query q = em.createQuery("SELECT COUNT(prod) FROM Product prod WHERE " + where);
        applyParams(q, params, textIds);
        return (Long) q.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    private List<Product> fetchPage(String where, SearchParams params, Set<Long> textIds) {
        String order = switch (params.sort() != null ? params.sort() : "newest") {
            case "price_asc"  -> " ORDER BY prod.price ASC";
            case "price_desc" -> " ORDER BY prod.price DESC";
            default           -> " ORDER BY prod.createdAt DESC";
        };
        Query q = em.createQuery(
                "SELECT prod FROM Product prod JOIN FETCH prod.category JOIN FETCH prod.seller WHERE " + where + order);
        applyParams(q, params, textIds);
        return q.setFirstResult(params.page() * params.size()).setMaxResults(params.size()).getResultList();
    }

    @SuppressWarnings("unchecked")
    private Map<Long, String> batchPrimaryImages(List<Long> productIds) {
        if (productIds.isEmpty()) return Map.of();
        List<Object[]> rows = em.createQuery(
                "SELECT i.product.id, i.path FROM ProductImage i WHERE i.product.id IN :ids AND i.isPrimary = true")
                .setParameter("ids", productIds).getResultList();
        return rows.stream().collect(Collectors.toMap(r -> (Long) r[0], r -> (String) r[1], (a, b) -> a));
    }
}
