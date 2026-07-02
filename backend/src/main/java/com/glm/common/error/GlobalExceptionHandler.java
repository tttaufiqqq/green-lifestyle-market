package com.glm.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

/**
 * Single place where error codes become HTTP responses (docs/error-catalogue.md rule).
 * No silent catches — every exception surfaces here.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError.Detail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.Detail(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ApiError apiError = new ApiError(ErrorCode.E_VAL.getCode(), "Validation failed", details, null);
        return ResponseEntity.badRequest().body(new ErrorResponse(apiError));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        log.warn("Domain error [{}]: {}", ex.getErrorCode().getCode(), ex.getMessage());
        ApiError apiError = new ApiError(ex.getErrorCode().getCode(), ex.getMessage(), List.of(), null);
        return ResponseEntity.status(ex.getHttpStatus()).body(new ErrorResponse(apiError));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unexpected error [{}]", errorId, ex);
        ApiError apiError = new ApiError(
                ErrorCode.E_INTERNAL.getCode(),
                "An unexpected error occurred. Quote errorId when contacting support.",
                List.of(),
                errorId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(apiError));
    }
}
