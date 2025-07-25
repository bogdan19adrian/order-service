package com.bogdanenache.order_service.dto;

import com.bogdanenache.order_service.dto.validation.OneOf;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record OrderDTO(
    String id,

    @NotNull
    String accountId,

    @NotNull
    String symbol,

    @NotNull
    @OneOf(enumClass = Side.class, message = "Side must be either BUY or SELL")
    String side,

    @NotNull
    @Min(value = 1)
    Integer quantity,

    String status,

    Instant createdAt,

    ExecutionDTO execution
) {

    public OrderDTO {
    }

}
