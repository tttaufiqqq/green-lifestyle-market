package com.glm.common.error;

/**
 * Base exception for all application domain errors.
 * Services throw this; GlobalExceptionHandler maps it to the error envelope.
 */
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final int httpStatus;

    public DomainException(ErrorCode errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
