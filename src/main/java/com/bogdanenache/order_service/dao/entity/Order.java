package com.bogdanenache.order_service.dao.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString
@AttributeOverride(name = "version", column = @Column(name = "ord_version"))
@Table(name = "ORDERS")
public class Order {

    @Id
    @GeneratedValue(generator = "ord_id_generator")
    @SequenceGenerator(name = "ord_id_generator", sequenceName = "SEQ_ORD_ID", allocationSize = 1)
    @Column(name = "ord_id", nullable = false, length = 10, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "ord_internal_id", length = 36, nullable = false)
    private String orderInternalId;

    @Column(name = "ord_account_id", length = 36, nullable = false)
    private String accountId;

    @Column(name = "ord_symbol", length = 20, nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "ord_side", length = 20, nullable = false)
    private OrderSide side;

    @Column(name = "ord_quantity", length = 20, nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "ord_status", length = 20, nullable = false)
    private OrderStatus status;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Execution execution;

    @Version
    @Column(name = "ord_version", length = 6)
    private int version;

    @Column(name = "ord_created")
    private Instant createdAt;


    public enum OrderStatus {
        PROCESSED,
        FAILED
    }

    public enum OrderSide {
        BUY,
        SELL
    }

}
