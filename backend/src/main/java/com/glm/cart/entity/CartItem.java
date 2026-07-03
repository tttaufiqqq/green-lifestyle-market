package com.glm.cart.entity;

import com.glm.catalog.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "cart_items",
    uniqueConstraints = @UniqueConstraint(name = "ci_cart_prod_uq",
        columnNames = {"cart_id", "product_id"}))
public class CartItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Column(name = "price_snapshot", precision = 10, scale = 2)
    private java.math.BigDecimal priceSnapshot;
}
