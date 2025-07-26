package com.bogdanenache.order_service.exception;

public class ConflictException extends RuntimeException {

    private final ErrorCode errorCode;

    public ConflictException(String message) {
        super(message);
        this.errorCode = ErrorCode.CONFLICT;
    }

    public ConflictException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ConflictException(Message message, ErrorCode errorCode) {
        this(message.getFormatMessage(), errorCode);
    }

    public ConflictException(String message, ErrorCode errorCode, Throwable e) {
        super(message, e);
        this.errorCode = errorCode;
    }

    public String getErrorCodeString() {
        return errorCode.name();
    }

    public enum Message {
        CONFLICT("Conflict.");

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

