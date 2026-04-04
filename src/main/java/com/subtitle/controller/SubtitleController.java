package com.subtitle.controller;

import com.subtitle.controller.dto.ApiResponse;
import com.subtitle.controller.dto.SubtitleResponse;
import com.subtitle.controller.dto.SubtitleUploadRequest;
import com.subtitle.service.SubtitleService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/subtitle")
@RequiredArgsConstructor
@Timed
public class SubtitleController {

    private final SubtitleService subtitleService;

    /**
     * Convert SRT file to Pinyin.
     */
    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SubtitleResponse>> convertToPinyin(
            @Valid @ModelAttribute SubtitleUploadRequest request) {

        SubtitleResponse response = subtitleService.convertSrtToPinyin(
                request.getFile(),
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("File successfully converted to Pinyin", response)
        );
    }

    /**
     * Download converted subtitle file.
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        return subtitleService.download(fileName);
    }

    /**
     * Validate SRT file.
     */
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> validateFile(
            @Valid @ModelAttribute SubtitleUploadRequest request) {

        subtitleService.validateSrtFile(request.getFile());

        return ResponseEntity.ok(
                ApiResponse.success("File is valid", null)
        );
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.success("Service is running", "OK")
        );
    }
}
