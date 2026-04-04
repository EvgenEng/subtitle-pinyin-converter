package com.subtitle.exception;

import com.subtitle.controller.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle BusinessException.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {

        ErrorCode error = ex.getErrorCode();

        log.warn("Business exception [{}]: {}", error.getCode(), ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                error.getCode(),
                error.getStatus()
        );

        return ResponseEntity.status(error.getStatus()).body(response);
    }

    /**
     * Handle validation exceptions (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", errors);

        ApiResponse<Void> response = ApiResponse.error(
                "Validation failed: " + errors,
                "VALIDATION_ERROR"
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle constraint violations.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex) {

        log.warn("Constraint violation: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                "Validation error: " + ex.getMessage(),
                "CONSTRAINT_VIOLATION"
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle file size exceeded.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxSizeException(
            MaxUploadSizeExceededException ex) {

        log.warn("File too large: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.FILE_TOO_LARGE.getMessage(),
                ErrorCode.FILE_TOO_LARGE.getCode(),
                ErrorCode.FILE_TOO_LARGE.getStatus()
        );

        return ResponseEntity
                .status(ErrorCode.FILE_TOO_LARGE.getStatus())
                .body(response);
    }

    /**
     * Handle multipart errors.
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(MultipartException ex) {

        log.warn("Multipart error: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                "File upload error: " + ex.getMessage(),
                "UPLOAD_ERROR"
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle IO errors.
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Void>> handleIOException(IOException ex) {

        log.error("IO error", ex);

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.FILE_READ_ERROR.getMessage(),
                ErrorCode.FILE_READ_ERROR.getCode(),
                ErrorCode.FILE_READ_ERROR.getStatus()
        );

        return ResponseEntity
                .status(ErrorCode.FILE_READ_ERROR.getStatus())
                .body(response);
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {

        log.error("Unexpected error", ex);

        ApiResponse<Void> response = ApiResponse.error(
                ErrorCode.INTERNAL_ERROR.getMessage(),
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getStatus()
        );

        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(response);
    }
}
