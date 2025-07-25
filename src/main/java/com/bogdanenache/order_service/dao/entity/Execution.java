package com.bogdanenache.order_service.dao.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Version;
import java.math.BigDecimal;
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
@AttributeOverride(name = "version", column = @Column(name = "exc_version"))
public class Execution {

    @Id
    @GeneratedValue(generator = "exc_id_generator")
    @SequenceGenerator(name = "exc_id_generator", sequenceName = "SEQ_EXC_ID", allocationSize = 1)
    @Column(name = "exc_id", nullable = false, length = 10, insertable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "exc_internal_id", length = 36, nullable = false)
    private String internalId;

    @Column(name = "ord_order_id", length = 10, nullable = false)
    private Long orderId;

    @Column(name = "exc_price", length = 20, nullable = false)
    private BigDecimal price;

    @OneToOne
    @JoinColumn(name = "ord_order_id", nullable = false)
    private Order order;

    @Version
    @Column(name = "exc_version", length = 6)
    private int version;

    @Column(name = "ord_created", insertable = false, updatable = false)
    private LocalDateTime created;

    @Column(name = "ord_last_updated", insertable = false, updatable = false)
    private LocalDateTime lastUpdated;
}
