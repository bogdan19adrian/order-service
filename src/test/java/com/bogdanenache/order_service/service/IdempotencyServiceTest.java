package com.bogdanenache.order_service.service;

import com.bogdanenache.order_service.dao.entity.Order;
import com.bogdanenache.order_service.dao.repository.OrderRepository;
import com.bogdanenache.order_service.exception.IdempotencyHeaderException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IdempotencyServiceTest {

    private OrderRepository orderRepository;
    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        idempotencyService = new IdempotencyService(orderRepository);
    }

    @Test
    void validateIdempotencyKey_shouldThrowException_whenKeyAlreadyExists() {
        String idempotencyKey = "12345678901234567890123456789012";
        Order order = new Order();
        order.setOrderInternalId("orderId");
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(order));

        IdempotencyHeaderException exception = assertThrows(IdempotencyHeaderException.class,
                () -> idempotencyService.validateIdempotencyKey(idempotencyKey));

        assertEquals("Idempotency key 12345678901234567890123456789012 is already used.", exception.getMessage());
    }

    @Test
    void validateIdempotencyKey_shouldLogMessage_whenKeyIsAvailable() {
        String idempotencyKey = "12345678901234567890123456789012";
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());

        idempotencyService.validateIdempotencyKey(idempotencyKey);

        verify(orderRepository).findByIdempotencyKey(idempotencyKey);
    }

    @Test
    void validateIdempotencyKey_shouldThrowException_whenKeyIsTooShort() {
        String idempotencyKey = "shortKey";

        IdempotencyHeaderException exception = assertThrows(IdempotencyHeaderException.class,
                () -> idempotencyService.validateIdempotencyKey(idempotencyKey));

        assertEquals("Idempotency key shortKey is invalid.", exception.getMessage());
    }

    @Test
    void validateIdempotencyKey_shouldThrowException_whenKeyIsTooLong() {
        String idempotencyKey = "1234567890123456789012345678901234567890";

        IdempotencyHeaderException exception = assertThrows(IdempotencyHeaderException.class,
                () -> idempotencyService.validateIdempotencyKey(idempotencyKey));

        assertEquals("Idempotency key 1234567890123456789012345678901234567890 is invalid.", exception.getMessage());
    }

    @Test
    void validateIdempotencyKey_shouldThrowException_whenKeyIsNull() {
        IdempotencyHeaderException exception = assertThrows(IdempotencyHeaderException.class,
                () -> idempotencyService.validateIdempotencyKey(null));

        assertEquals("Idempotency key null is invalid.", exception.getMessage());
    }

}
