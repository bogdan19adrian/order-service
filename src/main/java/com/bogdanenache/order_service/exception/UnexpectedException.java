package com.bogdanenache.order_service.exception;

public class UnexpectedException extends RuntimeException {

    private final ErrorCode errorCode;

    public UnexpectedException(String message) {
        super(message);
        this.errorCode = ErrorCode.BAD_REQUEST;
    }

    public UnexpectedException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public UnexpectedException(Message message, ErrorCode errorCode) {
        this(message.getFormatMessage(), errorCode);
    }

    public UnexpectedException(String message, ErrorCode errorCode, Throwable e) {
        super(message, e);
        this.errorCode = errorCode;
    }

    public String getErrorCodeString() {
        return errorCode.name();
    }

    public enum Message {
        FAILED_TO_FETCH_SYMBOL("Failed to fetch price for symbol: %s."),
        INVALID_RESPONSE("Invalid price feed response: missing data."),
        UNABLE_TO_CALL_PRICE_FEED("Service unavailable due to technical errors.");

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

