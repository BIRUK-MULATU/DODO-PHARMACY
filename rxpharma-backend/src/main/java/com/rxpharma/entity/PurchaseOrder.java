package com.rxpharma.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ordered_by")
    private User orderedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "order_status")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    private Status status;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "order_date", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseOrderItem> items = new java.util.ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
        if (this.status == null) this.status = Status.DRAFT;
    }

    public enum Status {
        DRAFT, SENT, DELIVERED, CANCELLED
    }
}