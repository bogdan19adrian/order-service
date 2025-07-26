package com.bogdanenache.order_service;

import com.bogdanenache.order_service.dto.OrderDTO;
import com.bogdanenache.order_service.dto.Side;
import java.util.UUID;

public class BaseTest {


    protected OrderDTO createOrder(Integer quantity, String symbol) {
        return new OrderDTO(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                symbol,
                Side.SELL.name(),
                quantity,
                null,
                null,
                null

            );
    }

}
