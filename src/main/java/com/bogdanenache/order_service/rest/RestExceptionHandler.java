package com.bogdanenache.order_service.rest;

import com.bogdanenache.order_service.dto.ErrorResponse;
import com.bogdanenache.order_service.exception.BadRequestException;
import com.bogdanenache.order_service.exception.ErrorCode;
import com.bogdanenache.order_service.exception.IdempotencyHeaderException;
import com.bogdanenache.order_service.exception.UnexpectedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for REST controllers.
 * Provides centralized handling of exceptions and maps them to appropriate HTTP responses.
 */
@RestControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles exceptions caused by method argument type mismatches.
     *
     * @param ex the exception thrown when a method argument type mismatch occurs
     * @return a ResponseEntity containing an error response with a bad request status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return ResponseEntity.badRequest().body(new ErrorResponse(ErrorCode.TYPE_MISMATCH.name(), message));
    }

    /**
     * Handles exceptions caused by constraint violations.
     *
     * @param ex the exception thrown when a constraint violation occurs
     * @return a ResponseEntity containing an error response with a bad request status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ErrorCode.CONSTRAINT_VIOLATION.name(), ex.getMessage()));
    }

    /**
     * Handles generic exceptions that are not explicitly handled by other methods.
     *
     * @param ex the exception thrown
     * @return a ResponseEntity containing an error response with an internal server error status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ErrorCode.INTERNAL_ERROR.name(), "An unexpected error occurred"));
    }

    /**
     * Handles UnexpectedException and maps it to a service unavailable response.
     *
     * @param ex the UnexpectedException thrown
     * @return a ResponseEntity containing an error response with a service unavailable status
     */
    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(ErrorCode.SERVICE_UNAVAILABLE.name(), "Service is currently unavailable"));
    }

    /**
     * Handles IdempotencyHeaderException and maps it to a service unavailable response.
     *
     * @param ex the IdempotencyHeaderException thrown
     * @return a ResponseEntity containing an error response with a bad request status
     */
    @ExceptionHandler(IdempotencyHeaderException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyHeaderException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ErrorCode.BAD_REQUEST.name(), ex.getMessage()));
    }

    /**
     * Handles BadRequestException and maps it to an unprocessable entity response.
     *
     * @param ex the BadRequestException thrown
     * @return a ResponseEntity containing an error response with an unprocessable entity status
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(ErrorCode.UNPROCESSABLE_ENTITY.name(), "An unexpected error occurred while processing the request"));
    }

    /**
     * Handles RequestNotPermitted exceptions caused by rate limiting.
     *
     * @param ex the RequestNotPermitted exception thrown
     * @return a ResponseEntity containing an error response with a too many requests status
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RequestNotPermitted ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(ErrorCode.TOO_MANY_REQUESTS.name(), "An unexpected error occurred while processing the request"));
    }

    /**
     * Handles validation errors for method arguments.
     *
     * @param ex the MethodArgumentNotValidException thrown
     * @param headers the HTTP headers
     * @param status the HTTP status code
     * @param request the web request
     * @return a ResponseEntity containing an error response with a bad request status
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = (fieldError != null)
                ? String.format("Invalid value for field '%s': %s", fieldError.getField(), fieldError.getDefaultMessage())
                : "Validation failed";

        return ResponseEntity.badRequest().body(new ErrorResponse(ErrorCode.VALIDATION_ERROR.name(), message));
    }

    /**
     * Handles missing servlet request parameter exceptions.
     *
     * @param ex the MissingServletRequestParameterException thrown
     * @param headers the HTTP headers
     * @param status the HTTP status code
     * @param request the web request
     * @return a ResponseEntity containing an error response with a bad request status
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ErrorCode.MISSING_PARAMETER.name(), ex.getMessage()));
    }

    /**
     * Handles exceptions caused by unreadable HTTP message bodies.
     *
     * @param ex the HttpMessageNotReadableException thrown
     * @param headers the HTTP headers
     * @param status the HTTP status code
     * @param request the web request
     * @return a ResponseEntity containing an error response with a bad request status
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ErrorCode.MALFORMED_JSON.name(), "Request body is not readable"));
    }
}