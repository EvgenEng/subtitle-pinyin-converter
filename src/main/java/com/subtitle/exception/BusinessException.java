package com.subtitle.exception;

import lombok.Getter;

/**
 * Custom business exception for application-specific errors.
 *
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] args;

    /**
     * Constructor with error code and default message.
     *
     * @param errorCode Error code
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.args = null;
    }

    /**
     * Constructor with message and error code.
     *
     * @param message Error message
     * @param errorCode Error code
     */
    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
        this.args = null;
    }

    /**
     * Constructor with message, error code, and arguments.
     *
     * @param message Error message
     * @param errorCode Error code
     * @param args Additional arguments
     */
    public BusinessException(String message, ErrorCode errorCode, Object... args) {
        super(message);
        this.errorCode = errorCode.getCode();
        this.args = args;
    }

    /**
     * Constructor with message, error code, and cause.
     *
     * @param message Error message
     * @param errorCode Error code
     * @param cause Original exception cause
     */
    public BusinessException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
        this.args = null;
    }
}
