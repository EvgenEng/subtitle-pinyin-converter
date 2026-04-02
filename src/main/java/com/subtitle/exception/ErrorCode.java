package com.subtitle.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Enumeration of error codes used throughout the application.
 *
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // File validation errors
    EMPTY_FILE("FILE_001", "File is empty", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE("FILE_002", "Invalid file type. Only SRT files are allowed", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("FILE_003", "File size exceeds maximum limit", HttpStatus.BAD_REQUEST),
    FILE_READ_ERROR("FILE_004", "Error reading file", HttpStatus.INTERNAL_SERVER_ERROR),

    // SRT parsing errors
    INVALID_SRT_FORMAT("SRT_001", "Invalid SRT format", HttpStatus.BAD_REQUEST),
    INVALID_TIMECODE("SRT_002", "Invalid timecode format", HttpStatus.BAD_REQUEST),
    MISSING_BLOCK_INDEX("SRT_003", "Missing block index", HttpStatus.BAD_REQUEST),

    // Pinyin conversion errors
    PINYIN_CONVERSION_ERROR("PIN_001", "Error converting to Pinyin", HttpStatus.INTERNAL_SERVER_ERROR),
    UNSUPPORTED_CHARACTER("PIN_002", "Unsupported character for Pinyin conversion", HttpStatus.BAD_REQUEST),

    // System errors
    INTERNAL_ERROR("SYS_001", "Internal system error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("SYS_002", "Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
