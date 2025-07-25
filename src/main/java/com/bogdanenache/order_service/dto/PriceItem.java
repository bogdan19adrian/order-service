package com.bogdanenache.order_service.dto;

import java.math.BigDecimal;

public record PriceItem(String symbol, BigDecimal price) {}
