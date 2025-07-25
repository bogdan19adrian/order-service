package com.bogdanenache.order_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ExecutionDTO(
    String internalId,
    Long orderId,
    BigDecimal price,
    Instant created
)  {}
