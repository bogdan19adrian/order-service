package com.bogdanenache.order_service.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.NOT_FOUND;
    }

    public ResourceNotFoundException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResourceNotFoundException(Message message, ErrorCode errorCode) {
        this(message.getFormatMessage(), errorCode);
    }

    public ResourceNotFoundException(String message, ErrorCode errorCode, Throwable e) {
        super(message, e);
        this.errorCode = errorCode;
    }

    public String getErrorCodeString() {
        return errorCode.name();
    }

    public enum Message {
        CONFLICT("Not Found.");

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

