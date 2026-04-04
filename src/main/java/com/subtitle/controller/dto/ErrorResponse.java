package com.subtitle.controller.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {

    private boolean success;
    private String message;
    private String errorCode;
    private HttpStatus status;
    private LocalDateTime timestamp;
}
