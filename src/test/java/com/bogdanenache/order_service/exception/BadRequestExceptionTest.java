package com.bogdanenache.order_service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadRequestExceptionTest {

    @Test
    void shouldReturnErrorCodeString_whenErrorCodeIsProvided() {
        BadRequestException exception = new BadRequestException("Invalid input", ErrorCode.BAD_REQUEST);
        assertEquals("BAD_REQUEST", exception.getErrorCodeString());
    }

    @Test
    void shouldReturnFormattedMessage_whenMessageEnumIsUsed() {
        BadRequestException exception = new BadRequestException(
                BadRequestException.Message.PRICE_NOT_FOUND_FOR_SYMBOL.with("AAPL"), ErrorCode.BAD_REQUEST);
        assertEquals("Price not found for symbol: AAPL.", exception.getMessage());
    }

    @Test
    void shouldReturnDefaultErrorCode_whenOnlyMessageIsProvided() {
        BadRequestException exception = new BadRequestException("Invalid input");
        assertEquals("BAD_REQUEST", exception.getErrorCodeString());
    }

    @Test
    void shouldReturnCause_whenThrowableIsProvided() {
        Throwable cause = new RuntimeException("Root cause");
        BadRequestException exception = new BadRequestException("Invalid input", ErrorCode.BAD_REQUEST, cause);
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldHandleNullMessageEnumGracefully() {
        BadRequestException exception = new BadRequestException((String) null, ErrorCode.BAD_REQUEST);
        assertNull(exception.getMessage());
    }
}