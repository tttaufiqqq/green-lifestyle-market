package com.glm.cart;

import com.glm.cart.dto.*;
import com.glm.cart.entity.Cart;
import com.glm.cart.entity.CartItem;
import com.glm.cart.repository.CartItemRepository;
import com.glm.catalog.entity.Product;
import com.glm.order.entity.StockReservation;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds CartView from DB state: groups by seller, computes live availability,
 * detects price changes via price_snapshot column (V7 migration).
 */
@Component
public class CartLoader {

    private final CartItemRepository cartItemRepo;
    private final EntityManager em;

    public CartLoader(CartItemRepository cartItemRepo, EntityManager em) {
        this.cartItemRepo = cartItemRepo;
        this.em = em;
    }

    @Transactional(readOnly = true)
    public CartView buildView(Cart cart) {
        List<CartItem> items = cartItemRepo.findByCartId(cart.getId());
        if (items.isEmpty()) return new CartView(List.of(), 0, false);

        List<Long> productIds = items.stream().map(ci -> ci.getProduct().getId()).toList();

        Map<Long, Integer> heldMap   = batchHeld(productIds);
        Map<Long, String>  imageMap  = batchImages(productIds);

        // LinkedHashMap preserves insertion order for seller groups
        Map<Long, List<CartItemView>> bySeller     = new LinkedHashMap<>();
        Map<Long, String>             sellerNames  = new LinkedHashMap<>();
        Map<Long, boolean[]>          fulfilFlags  = new LinkedHashMap<>(); // [allowMeetup, allowShipping]
        boolean hasWarnings = false;
        int totalItems = 0;

        for (CartItem ci : items) {
            Product    p         = ci.getProduct();
            int        held      = heldMap.getOrDefault(p.getId(), 0);
            int        available = Math.max(0, p.getQuantity() - held);
            BigDecimal current   = p.getPrice();
            BigDecimal snapshot  = ci.getPriceSnapshot();

            boolean priceChanged = snapshot != null && snapshot.compareTo(current) != 0;
            boolean outOfStock   = available <= 0;
            if (priceChanged || outOfStock) hasWarnings = true;

            CartItemView view = new CartItemView(
                ci.getId(), p.getId(), p.getTitle(), p.getSlug(),
                imageMap.get(p.getId()), current, snapshot,
                ci.getQuantity(), available, priceChanged, outOfStock
            );

            Long   sellerId   = p.getSeller().getId();
            String sellerName = p.getSeller().getName();
            sellerNames.putIfAbsent(sellerId, sellerName);
            bySeller.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(view);
            totalItems += ci.getQuantity();

            // Aggregate fulfilment flags: group allows method only if ALL items support it
            boolean[] flags = fulfilFlags.computeIfAbsent(sellerId, k -> new boolean[]{true, true});
            if (!p.isAllowMeetup())   flags[0] = false;
            if (!p.isAllowShipping()) flags[1] = false;
        }

        List<SellerGroupView> groups = bySeller.entrySet().stream()
            .map(e -> {
                boolean[] f = fulfilFlags.getOrDefault(e.getKey(), new boolean[]{true, false});
                return new SellerGroupView(e.getKey(), sellerNames.get(e.getKey()), e.getValue(), f[0], f[1]);
            })
            .toList();

        return new CartView(groups, totalItems, hasWarnings);
    }

    /** Batch HELD reservation counts per product. One query. */
    @SuppressWarnings("unchecked")
    private Map<Long, Integer> batchHeld(List<Long> ids) {
        List<Object[]> rows = em.createQuery(
            "SELECT sr.product.id, SUM(sr.quantity) FROM StockReservation sr " +
            "WHERE sr.product.id IN :ids AND sr.status = :held GROUP BY sr.product.id")
            .setParameter("ids", ids)
            .setParameter("held", StockReservation.Status.HELD)
            .getResultList();
        return rows.stream().collect(Collectors.toMap(
            r -> (Long) r[0], r -> ((Number) r[1]).intValue()));
    }

    /** Batch primary images per product. One query. */
    @SuppressWarnings("unchecked")
    private Map<Long, String> batchImages(List<Long> ids) {
        List<Object[]> rows = em.createQuery(
            "SELECT i.product.id, i.path FROM ProductImage i " +
            "WHERE i.product.id IN :ids AND i.isPrimary = true")
            .setParameter("ids", ids)
            .getResultList();
        return rows.stream().collect(Collectors.toMap(
            r -> (Long) r[0], r -> (String) r[1], (a, b) -> a));
    }
}
