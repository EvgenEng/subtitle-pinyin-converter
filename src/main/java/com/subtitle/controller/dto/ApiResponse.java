package com.subtitle.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Generic API response wrapper for consistent response structure.
 *
 * @param <T> Type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private HttpStatus status;

    /**
     * Create a success response with data.
     *
     * @param data Response data
     * @param <T> Type of data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .status(HttpStatus.OK)
                .build();
    }

    /**
     * Create a success response with message and data.
     *
     * @param message Success message
     * @param data Response data
     * @param <T> Type of data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(HttpStatus.OK)
                .build();
    }

    /**
     * Create an error response.
     *
     * @param message Error message
     * @param errorCode Error code
     * @param <T> Type placeholder
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .status(HttpStatus.BAD_REQUEST)
                .build();
    }

    /**
     * Create an error response with specific status.
     *
     * @param message Error message
     * @param errorCode Error code
     * @param status HTTP status
     * @param <T> Type placeholder
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, HttpStatus status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .status(status)
                .build();
    }
}
