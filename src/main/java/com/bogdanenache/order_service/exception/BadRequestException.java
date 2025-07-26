package com.bogdanenache.order_service.exception;

public class BadRequestException extends RuntimeException {

    private final ErrorCode errorCode;

    public BadRequestException(String message) {
        super(message);
        this.errorCode = ErrorCode.BAD_REQUEST;
    }

    public BadRequestException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BadRequestException(Message message, ErrorCode errorCode) {
        this(message.getFormatMessage(), errorCode);
    }

    public BadRequestException(String message, ErrorCode errorCode, Throwable e) {
        super(message, e);
        this.errorCode = errorCode;
    }

    public String getErrorCodeString() {
        return errorCode.name();
    }

    public enum Message {
        PRICE_NOT_FOUND_FOR_SYMBOL("Price not found for symbol: %s.");

        final String msg;

        Message(String msg) {
            this.msg = msg;
        }

        public String getFormatMessage() {
            return msg;
        }

        public String with(Object... formatParams) {
            return String.format(msg, formatParams);
        }
    }
}

