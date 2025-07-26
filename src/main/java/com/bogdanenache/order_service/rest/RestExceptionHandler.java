package com.bogdanenache.order_service.rest;

import com.bogdanenache.order_service.dto.ErrorResponse;
import com.bogdanenache.order_service.exception.ConflictException;
import com.bogdanenache.order_service.exception.ErrorCode;
import com.bogdanenache.order_service.exception.ResourceNotFoundException;
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


    // 400 - Type mismatch (e.g. passing string instead of int)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return ResponseEntity.badRequest().body(new ErrorResponse("TYPE_MISMATCH", message));
    }

    // 400 - Constraint violation (e.g. query param validation)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("CONSTRAINT_VIOLATION", ex.getMessage()));
    }

    // 404 - Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ErrorCode.NOT_FOUND.name(), ex.getMessage()));
    }

    // 409 - Conflict (e.g. idempotency, duplicate key)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ErrorCode.CONFLICT.name(), ex.getMessage()));
    }

    // 500 - Generic fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    // 400 - Validation or malformed body
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

        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    // 400 - Missing query/path/body parameters
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {
        return ResponseEntity.badRequest().body(new ErrorResponse("MISSING_PARAMETER", ex.getMessage()));
    }

    // 400 - Invalid body format (e.g. malformed JSON)
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request
    ) {
        return ResponseEntity.badRequest().body(new ErrorResponse("MALFORMED_JSON", "Request body is not readable"));
    }
}

