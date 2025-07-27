package com.bogdanenache.order_service.exception;

/**
 * Custom exception to represent unexpected scenarios.
 * This exception is typically thrown when an unexpected error occurs in the application.
 */
public class UnexpectedException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * Constructs a new UnexpectedException with the specified message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public UnexpectedException(String message) {
        super(message);
        this.errorCode = ErrorCode.BAD_REQUEST;
    }

    /**
     * Constructs a new UnexpectedException with the specified message and error code.
     *
     * @param message   the detail message explaining the reason for the exception
     * @param errorCode the specific error code associated with this exception
     */
    public UnexpectedException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new UnexpectedException with a formatted message from the Message enum and an error code.
     *
     * @param message   the predefined message enum containing the format string
     * @param errorCode the specific error code associated with this exception
     */
    public UnexpectedException(Message message, ErrorCode errorCode) {
        this(message.getFormatMessage(), errorCode);
    }

    /**
     * Constructs a new UnexpectedException with the specified message, error code, and cause.
     *
     * @param message   the detail message explaining the reason for the exception
     * @param errorCode the specific error code associated with this exception
     * @param e         the cause of the exception
     */
    public UnexpectedException(String message, ErrorCode errorCode, Throwable e) {
        super(message, e);
        this.errorCode = errorCode;
    }

    /**
     * Retrieves the name of the error code associated with this exception.
     *
     * @return the name of the error code as a string
     */
    public String getErrorCodeString() {
        return errorCode.name();
    }

    /**
     * Enum representing predefined messages for UnexpectedException.
     * Each message can be formatted with additional parameters.
     */
    public enum Message {
        FAILED_TO_FETCH_SYMBOL("Failed to fetch price for symbol: %s."),
        INVALID_RESPONSE("Invalid price feed response: missing data."),
        UNABLE_TO_CALL_PRICE_FEED("Service unavailable due to technical errors.");

        final String msg;

        /**
         * Constructs a Message enum with the specified format string.
         *
         * @param msg the format string for the message
         */
        Message(String msg) {
            this.msg = msg;
        }

        /**
         * Retrieves the format string of the message.
         *
         * @return the format string
         */
        public String getFormatMessage() {
            return msg;
        }

        /**
         * Formats the message with the specified parameters.
         *
         * @param formatParams the parameters to format the message
         * @return the formatted message as a string
         */
        public String with(Object... formatParams) {
            return String.format(msg, formatParams);
        }
    }
}