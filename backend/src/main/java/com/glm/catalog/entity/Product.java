package com.glm.catalog.entity;

import com.glm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "products")
public class Product {

    public enum ItemCondition { NEW, LIKE_NEW, GOOD, FAIR }
    public enum Status { DRAFT, ACTIVE, SOLD_OUT, SUSPENDED, DELETED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", length = 120, nullable = false)
    private String title;

    @Column(name = "slug", length = 140, nullable = false, unique = true)
    private String slug;

    @Lob @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition", length = 10, nullable = false)
    private ItemCondition itemCondition;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Column(name = "allow_meetup", nullable = false)
    private boolean allowMeetup = true;

    @Column(name = "allow_shipping", nullable = false)
    private boolean allowShipping;

    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "meetup_location", length = 120)
    private String meetupLocation;

    @Column(name = "sustainability_note", length = 255)
    private String sustainabilityNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private Status status = Status.DRAFT;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant updatedAt;
}
