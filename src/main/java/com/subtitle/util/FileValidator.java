package com.subtitle.util;

import com.subtitle.exception.BusinessException;
import com.subtitle.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for file validation.
 *
 */
@Slf4j
@Component
public class FileValidator {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".srt", ".SRT");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "text/plain",
            "application/x-subrip",
            "text/x-srt"
    );

    @Value("${subtitle.max-file-size:10485760}") // 10MB default
    private long maxFileSize;

    /**
     * Validate uploaded file.
     *
     * @param file File to validate
     * @throws BusinessException if validation fails
     * @throws IOException if file reading fails
     */
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
                    String.format("File size %d exceeds maximum allowed %d bytes",
                            file.getSize(), maxFileSize),
                    ErrorCode.FILE_TOO_LARGE
            );
        }
    }

    private void validateFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("Invalid filename", ErrorCode.INVALID_FILE_TYPE);
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(
                    "Invalid file extension: " + extension,
                    ErrorCode.INVALID_FILE_TYPE
            );
        }
    }

    private void validateContent(MultipartFile file) throws IOException {
        // Check if file contains valid SRT format
        byte[] bytes = file.getBytes();
        String content = new String(bytes, "UTF-8");

        if (!content.contains("-->")) {
            throw new BusinessException(
                    "File does not appear to be a valid SRT file",
                    ErrorCode.INVALID_SRT_FORMAT
            );
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
