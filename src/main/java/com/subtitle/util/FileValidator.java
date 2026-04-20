package com.subtitle.util;

import com.subtitle.exception.BusinessException;
import com.subtitle.exception.ErrorCode;
import com.subtitle.parser.SrtParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileValidator {

    private final SrtParser srtParser;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".srt");

    @Value("${subtitle.max-file-size:10485760}")
    private long maxFileSize;

    public void validate(MultipartFile file) throws IOException {
        validateNotNull(file);
        validateEmpty(file);
        validateFileSize(file);
        validateFileExtension(file);
        validateContent(file);
    }

    private void validateNotNull(MultipartFile file) {
        if (file == null) {
            throw new BusinessException("File is null", ErrorCode.EMPTY_FILE);
        }
    }

    private void validateEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty", ErrorCode.EMPTY_FILE);
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(
                    "File too large",
                    ErrorCode.FILE_TOO_LARGE
            );
        }
    }

    private void validateFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException("Invalid filename", ErrorCode.INVALID_FILE_TYPE);
        }

        String extension = getFileExtension(originalFilename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(
                    "Invalid file extension: " + extension,
                    ErrorCode.INVALID_FILE_TYPE
            );
        }
    }

    private void validateContent(MultipartFile file) throws IOException {

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        if (!srtParser.isValidSrt(content)) {
            throw new BusinessException(
                    "Invalid SRT structure",
                    ErrorCode.INVALID_SRT_FORMAT
            );
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex);
    }
}
