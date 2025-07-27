package com.bogdanenache.order_service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnexpectedExceptionTest {

    @Test
    void shouldReturnErrorCodeString_whenErrorCodeIsProvided() {
        UnexpectedException exception = new UnexpectedException("Service failed", ErrorCode.INTERNAL_SERVER_ERROR);
        assertEquals("INTERNAL_SERVER_ERROR", exception.getErrorCodeString());
    }

    @Test
    void shouldReturnFormattedMessage_whenMessageEnumIsUsed() {
        UnexpectedException exception = new UnexpectedException(
                UnexpectedException.Message.FAILED_TO_FETCH_SYMBOL.with("AAPL"), ErrorCode.INTERNAL_SERVER_ERROR);
        assertEquals("Failed to fetch price for symbol: AAPL.", exception.getMessage());
    }

    @Test
    void shouldReturnDefaultErrorCode_whenOnlyMessageIsProvided() {
        UnexpectedException exception = new UnexpectedException("Service failed");
        assertEquals("BAD_REQUEST", exception.getErrorCodeString());
    }

    @Test
    void shouldReturnCause_whenThrowableIsProvided() {
        Throwable cause = new RuntimeException("Root cause");
        UnexpectedException exception = new UnexpectedException("Service failed", ErrorCode.INTERNAL_SERVER_ERROR, cause);
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldHandleNullMessageEnumGracefully() {
        UnexpectedException exception = new UnexpectedException((String) null, ErrorCode.INTERNAL_SERVER_ERROR);
        assertNull(exception.getMessage());
    }
}