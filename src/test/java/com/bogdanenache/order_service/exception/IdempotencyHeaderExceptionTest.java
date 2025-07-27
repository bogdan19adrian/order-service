package com.bogdanenache.order_service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdempotencyHeaderExceptionTest {

    @Test
    void shouldReturnErrorCodeString_whenErrorCodeIsProvided() {
        IdempotencyHeaderException exception = new IdempotencyHeaderException("Invalid input", ErrorCode.BAD_REQUEST);
        assertEquals("BAD_REQUEST", exception.getErrorCodeString());
    }

    @Test
    void shouldReturnFormattedMessage_whenMessageEnumIsUsed() {
        IdempotencyHeaderException exception = new IdempotencyHeaderException(
                IdempotencyHeaderException.Message.USED_IDEMPOTENCY_KEY.with("12345"), ErrorCode.BAD_REQUEST);
        assertEquals("Idempotency key 12345 is already used.", exception.getMessage());
    }

    @Test
    void shouldReturnDefaultErrorCode_whenOnlyMessageIsProvided() {
        IdempotencyHeaderException exception = new IdempotencyHeaderException("Invalid input");
        assertEquals("BAD_REQUEST", exception.getErrorCodeString());
    }

    @Test
    void shouldReturnCause_whenThrowableIsProvided() {
        Throwable cause = new RuntimeException("Root cause");
        IdempotencyHeaderException exception = new IdempotencyHeaderException("Invalid input", ErrorCode.BAD_REQUEST, cause);
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldHandleNullMessageEnumGracefully() {
        IdempotencyHeaderException exception = new IdempotencyHeaderException((String) null, ErrorCode.BAD_REQUEST);
        assertNull(exception.getMessage());
    }
}