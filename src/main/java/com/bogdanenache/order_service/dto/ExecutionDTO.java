package com.bogdanenache.order_service.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ExecutionDTO(
    String id,
    String orderId,
    BigDecimal price,
    Instant createdAt
)  {

    public ExecutionDTO {
    }

}
