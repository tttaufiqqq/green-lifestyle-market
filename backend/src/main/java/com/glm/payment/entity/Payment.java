package com.glm.payment.entity;

import com.glm.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor
@Entity @Table(name = "payments")
public class Payment {

    public enum Status { PENDING, SUCCESS, FAILED, EXPIRED, REVIEW }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_no", length = 15, nullable = false, unique = true)
    private String paymentNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "toyyibpay_bill_code", length = 20, unique = true)
    private String toyyibpayBillCode;

    @Column(name = "toyyibpay_ref_no", length = 30)
    private String toyyibpayRefNo;

    @Column(name = "verified_at", columnDefinition = "TIMESTAMP")
    private Instant verifiedAt;

    @Column(name = "expires_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant expiresAt;

    @Column(name = "paid_at", columnDefinition = "TIMESTAMP")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant updatedAt;
}
