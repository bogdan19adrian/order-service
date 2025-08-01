package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.dao.repository.OrderRepository;
import com.bogdanenache.order_service.exception.IdempotencyHeaderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.bogdanenache.order_service.exception.IdempotencyHeaderException.Message.INVALID_IDEMPOTENCY_KEY;
import static com.bogdanenache.order_service.exception.IdempotencyHeaderException.Message.USED_IDEMPOTENCY_KEY;

/**
 * Service class for handling idempotency key validation logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final OrderRepository orderRepository;

    /**
     * Validates the provided idempotency key.
     * Ensures the key is of valid length and checks if it already exists in the repository.
     * Logs appropriate messages and throws exceptions for invalid or duplicate keys.
     *
     * @param idempotencyKey the idempotency key to validate
     * @throws IdempotencyHeaderException if the key is invalid or already exists
     */
    public void validateIdempotencyKey(String idempotencyKey) {
        validateKeyLength(idempotencyKey);
        orderRepository.findByIdempotencyKey(idempotencyKey)
                .ifPresent(order -> {
                    log.error("Idempotency key {} already exists for order {}", idempotencyKey, order.getOrderInternalId());
                    throw new IdempotencyHeaderException(USED_IDEMPOTENCY_KEY.with(idempotencyKey));
                });
        log.info("Idempotency key {} is available for new order", idempotencyKey);
    }

    /**
     * Validates the length of the idempotency key.
     * Ensures the key is not null and its length is between 30 and 36 characters.
     *
     * @param idempotencyKey the idempotency key to validate
     * @throws IdempotencyHeaderException if the key is null or its length is invalid
     */
    private void validateKeyLength(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.length() < 30 || idempotencyKey.length() > 36) {
            throw new IdempotencyHeaderException(INVALID_IDEMPOTENCY_KEY.with(idempotencyKey));
        }
    }

}