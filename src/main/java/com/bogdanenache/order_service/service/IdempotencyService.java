package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.dao.repository.OrderRepository;
import com.bogdanenache.order_service.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.bogdanenache.order_service.exception.BadRequestException.Message.USED_IDEMPOTENCY_KEY;


@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final OrderRepository orderRepository;

    public void validateIdempotencyKey(String idempotencyKey) {
        orderRepository.findByIdempotencyKey(idempotencyKey)
                .ifPresent(order -> {
                    log.error("Idempotency key {} already exists for order {}", idempotencyKey, order.getOrderInternalId());
                    throw new BadRequestException(USED_IDEMPOTENCY_KEY.with(idempotencyKey));
                });
        log.info("Idempotency key {} is available for new order", idempotencyKey);
    }

}
