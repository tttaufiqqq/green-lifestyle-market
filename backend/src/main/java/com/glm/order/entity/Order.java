package com.glm.order.entity;

import com.glm.payment.entity.Payment;
import com.glm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "orders")
public class Order {

    public enum Status {
        PENDING_PAYMENT, PAID, CONFIRMED, SHIPPED, READY_FOR_MEETUP,
        COMPLETED, CANCELLED, EXPIRED, REFUND_REQUESTED, REFUNDED
    }
    public enum FulfilmentMethod { MEETUP, SHIPPING }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", length = 15, nullable = false, unique = true)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfilment_method", length = 10, nullable = false)
    private FulfilmentMethod fulfilmentMethod;

    @Column(name = "ship_name",     length = 100) private String shipName;
    @Column(name = "ship_phone",    length = 20)  private String shipPhone;
    @Column(name = "ship_address1", length = 255) private String shipAddress1;
    @Column(name = "ship_address2", length = 255) private String shipAddress2;
    @Column(name = "ship_postcode", length = 10)  private String shipPostcode;
    @Column(name = "ship_city",     length = 80)  private String shipCity;
    @Column(name = "ship_state",    length = 40)  private String shipState;
    @Column(name = "meetup_location", length = 120) private String meetupLocation;
    @Column(name = "meetup_note",     length = 255) private String meetupNote;
    @Column(name = "tracking_no",     length = 40)  private String trackingNo;
    @Column(name = "courier",         length = 40)  private String courier;

    @Column(name = "subtotal",     precision = 10, scale = 2, nullable = false) private BigDecimal subtotal;
    @Column(name = "shipping_fee", precision = 10, scale = 2, nullable = false) private BigDecimal shippingFee;
    @Column(name = "total",        precision = 10, scale = 2, nullable = false) private BigDecimal total;
    @Column(name = "platform_fee", precision = 10, scale = 2, nullable = false) private BigDecimal platformFee;
    @Column(name = "seller_net",   precision = 10, scale = 2, nullable = false) private BigDecimal sellerNet;

    @Column(name = "cancelled_reason", length = 255) private String cancelledReason;

    @Column(name = "confirmed_at",  columnDefinition = "TIMESTAMP") private Instant confirmedAt;
    @Column(name = "shipped_at",    columnDefinition = "TIMESTAMP") private Instant shippedAt;
    @Column(name = "completed_at",  columnDefinition = "TIMESTAMP") private Instant completedAt;
    @Column(name = "cancelled_at",  columnDefinition = "TIMESTAMP") private Instant cancelledAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP") private Instant createdAt;
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP") private Instant updatedAt;
}
