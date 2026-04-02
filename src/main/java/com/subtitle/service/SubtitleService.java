package com.subtitle.service;

import com.subtitle.controller.dto.SubtitleResponse;
import com.subtitle.controller.dto.SubtitleUploadRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for subtitle processing operations.
 */
public interface SubtitleService {

    SubtitleResponse convertSrtToPinyin(MultipartFile file, SubtitleUploadRequest request);

    void validateSrtFile(MultipartFile file);

    String getFileStatistics(String fileName);

    ResponseEntity<Resource> download(String fileName);
}
