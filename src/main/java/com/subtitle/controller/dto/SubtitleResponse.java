package com.subtitle.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for converted subtitle.
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleResponse {

    private String fileName;
    private String originalFileName;
    private String convertedContent;
    private long originalSize;
    private long convertedSize;
    private int blockCount;
    private LocalDateTime conversionTime;
    private String downloadUrl;
}
