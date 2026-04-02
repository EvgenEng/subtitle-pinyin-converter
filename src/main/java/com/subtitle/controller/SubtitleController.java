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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/subtitle")
@RequiredArgsConstructor
@Timed
public class SubtitleController {

    private final SubtitleService subtitleService;

    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SubtitleResponse>> convertToPinyin(
            @Valid @ModelAttribute SubtitleUploadRequest request) {

        //1. Проверка на пустой файл
        if (request.getFile() == null || request.getFile().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("File cannot be empty", "FILE_EMPTY")
            );
        }

        //2. Проверка расширения файла
        String fileName = request.getFile().getOriginalFilename();

        if (fileName == null || !fileName.toLowerCase().endsWith(".srt")) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Only .srt files are allowed", "INVALID_FILE_TYPE")
            );
        }

        SubtitleResponse response = subtitleService.convertSrtToPinyin(
                request.getFile(),
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("File successfully converted to Pinyin", response)
        );
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        return subtitleService.download(fileName);
    }

    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> validateFile(
            @Valid @ModelAttribute SubtitleUploadRequest request) {

        subtitleService.validateSrtFile(request.getFile());

        return ResponseEntity.ok(
                ApiResponse.success("File is valid", null)
        );
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.success("Service is running", "OK")
        );
    }
}
