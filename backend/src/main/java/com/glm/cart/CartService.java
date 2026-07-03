package com.glm.cart;

import com.glm.cart.dto.CartView;
import com.glm.cart.entity.Cart;
import com.glm.cart.entity.CartItem;
import com.glm.cart.repository.CartItemRepository;
import com.glm.cart.repository.CartRepository;
import com.glm.catalog.entity.Product;
import com.glm.catalog.repository.ProductRepository;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.entity.StockReservation;
import com.glm.user.entity.User;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CartService {

    private final CartRepository        cartRepo;
    private final CartItemRepository    cartItemRepo;
    private final ProductRepository     productRepo;
    private final CartValidator         validator;
    private final CartLoader            loader;
    private final EntityManager         em;

    public CartService(CartRepository cartRepo, CartItemRepository cartItemRepo,
                       ProductRepository productRepo, CartValidator validator,
                       CartLoader loader, EntityManager em) {
        this.cartRepo     = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.productRepo  = productRepo;
        this.validator    = validator;
        this.loader       = loader;
        this.em           = em;
    }

    @Transactional(readOnly = true)
    public CartView getCart(User buyer) {
        Cart cart = cartRepo.findByBuyerId(buyer.getId())
            .orElse(null);
        if (cart == null) return new CartView(java.util.List.of(), 0, false);
        return loader.buildView(cart);
    }

    /** Upsert: new → reject if qty > available; existing → cap silently (R05-A1). */
    @Transactional
    public CartView addItem(User buyer, Long productId, int quantity) {
        Product p = productRepo.findById(productId)
            .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Product not found", 404));

        validator.requireActive(p);
        validator.requireNotOwn(p, buyer);

        Cart cart = getOrCreateCart(buyer);

        int held      = computeHeld(productId);
        int available = Math.max(0, p.getQuantity() - held);

        cartItemRepo.findByCartIdAndProductId(cart.getId(), productId)
            .ifPresentOrElse(existing -> {
                int newQty = Math.min(existing.getQuantity() + quantity, available);
                existing.setQuantity(newQty);
                existing.setPriceSnapshot(p.getPrice());
                cartItemRepo.save(existing);
            }, () -> {
                validator.requireStock(quantity, available);
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setProduct(p);
                item.setQuantity(quantity);
                item.setPriceSnapshot(p.getPrice());
                cartItemRepo.save(item);
            });

        cart.setUpdatedAt(Instant.now());
        cartRepo.save(cart);
        return loader.buildView(cart);
    }

    @Transactional
    public CartView updateQuantity(User buyer, Long itemId, int quantity) {
        CartItem item = requireOwnItem(itemId, buyer);
        Product  p    = item.getProduct();

        int held      = computeHeld(p.getId());
        int available = Math.max(0, p.getQuantity() - held);

        item.setQuantity(Math.min(quantity, available));
        item.setPriceSnapshot(p.getPrice());
        cartItemRepo.save(item);

        Cart cart = item.getCart();
        cart.setUpdatedAt(Instant.now());
        cartRepo.save(cart);
        return loader.buildView(cart);
    }

    @Transactional
    public CartView removeItem(User buyer, Long itemId) {
        CartItem item = requireOwnItem(itemId, buyer);
        Cart     cart = item.getCart();
        cartItemRepo.delete(item);
        cart.setUpdatedAt(Instant.now());
        cartRepo.save(cart);
        return loader.buildView(cart);
    }

    /** Count of distinct cart item rows — used as badge count in /auth/me. */
    @Transactional(readOnly = true)
    public int getCartCount(Long userId) {
        return cartRepo.findByBuyerId(userId)
            .map(c -> cartItemRepo.findByCartId(c.getId()).size())
            .orElse(0);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Cart getOrCreateCart(User buyer) {
        return cartRepo.findByBuyerId(buyer.getId()).orElseGet(() -> {
            Cart c = new Cart();
            c.setBuyer(buyer);
            c.setCreatedAt(Instant.now());
            c.setUpdatedAt(Instant.now());
            return cartRepo.save(c);
        });
    }

    private CartItem requireOwnItem(Long itemId, User buyer) {
        CartItem item = cartItemRepo.findById(itemId)
            .orElseThrow(() -> new DomainException(ErrorCode.E_NOTFOUND, "Cart item not found", 404));
        Long ownerId = (Long) em.createQuery(
                "SELECT ci.cart.buyer.id FROM CartItem ci WHERE ci.id = :id")
            .setParameter("id", itemId)
            .getSingleResult();
        if (!ownerId.equals(buyer.getId()))
            throw new DomainException(ErrorCode.E_AUTH_OWN, "Not your cart item", 403);
        return item;
    }

    private int computeHeld(Long productId) {
        Number n = (Number) em.createQuery(
                "SELECT COALESCE(SUM(sr.quantity), 0) FROM StockReservation sr " +
                "WHERE sr.product.id = :pid AND sr.status = :held")
            .setParameter("pid", productId)
            .setParameter("held", StockReservation.Status.HELD)
            .getSingleResult();
        return n.intValue();
    }
}
