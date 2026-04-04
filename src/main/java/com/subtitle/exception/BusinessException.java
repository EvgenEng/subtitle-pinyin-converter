package com.subtitle.exception;

import lombok.Getter;

/**
 * Custom business exception for application-specific errors.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] args;

    /**
     * Constructor with ErrorCode.
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * Constructor with custom message and ErrorCode.
     */
    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    /**
     * Constructor with message, error code, and arguments.
     */
    public BusinessException(String message, ErrorCode errorCode, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * Constructor with cause.
     */
    public BusinessException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }
}
