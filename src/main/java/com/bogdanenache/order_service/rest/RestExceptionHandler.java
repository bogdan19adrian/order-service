package com.bogdanenache.order_service.rest;

import com.bogdanenache.order_service.dto.ErrorResponse;
import com.bogdanenache.order_service.exception.BadRequestException;
import com.bogdanenache.order_service.exception.ErrorCode;
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

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return ResponseEntity.badRequest().body(new ErrorResponse(ErrorCode.TYPE_MISMATCH.name(), message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ErrorCode.CONSTRAINT_VIOLATION.name(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ErrorCode.INTERNAL_ERROR.name(), "An unexpected error occurred"));
    }

    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<ErrorResponse> UnexpectedException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(ErrorCode.SERVICE_UNAVAILABLE.name(), "Service is currently unavailable"));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(ErrorCode.UNPROCESSABLE_ENTITY.name(), "An unexpected error occurred while processing the request"));
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RequestNotPermitted ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(ErrorCode.TOO_MANY_REQUESTS.name(), "An unexpected error occurred while processing the request"));
    }

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

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ErrorCode.MISSING_PARAMETER.name(), ex.getMessage()));
    }

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
