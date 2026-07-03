package com.glm.cart;

import com.glm.catalog.entity.Product;
import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * Validates pre-conditions before cart mutations.
 * Throws DomainException; caller's @Transactional rolls back automatically.
 */
@Component
public class CartValidator {

    /** Product must be ACTIVE to be added to cart. */
    void requireActive(Product p) {
        if (p.getStatus() != Product.Status.ACTIVE) {
            throw new DomainException(ErrorCode.E_NOTFOUND, "Product not available", 404);
        }
    }

    /** Seller cannot buy their own product (DR-4). */
    void requireNotOwn(Product p, User buyer) {
        if (p.getSeller().getId().equals(buyer.getId())) {
            throw new DomainException(ErrorCode.E_CART_OWN,
                    "You cannot buy your own item", 422);
        }
    }

    /**
     * For new cart items only — quantity must not exceed available stock.
     * Merged items are silently capped instead (upsert semantics per R05-A1).
     */
    void requireStock(int requested, int available) {
        if (requested > available) {
            throw new DomainException(ErrorCode.E_CART_STOCK,
                    "Only " + available + " left in stock", 409);
        }
        if (available <= 0) {
            throw new DomainException(ErrorCode.E_CART_STOCK, "Item is out of stock", 409);
        }
    }
}
