package com.subtitle.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for subtitle file upload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleUploadRequest {

    @NotNull(message = "File cannot be null")
    private MultipartFile file;

    @Builder.Default
    private boolean withToneMarks = true;

    @Builder.Default
    private PinyinFormat pinyinFormat = PinyinFormat.WITH_SPACES;

    @Builder.Default
    private boolean convertNonChinese = false;

    /**
     * Subtitle output mode
     */
    @Builder.Default
    private SubtitleMode mode = SubtitleMode.PINYIN;

    public enum PinyinFormat {
        WITH_SPACES,
        WITHOUT_SPACES,
        WITH_TONES,
        CAPITALIZED
    }

    public enum SubtitleMode {
        ORIGINAL,
        PINYIN,
        DUAL,
        TRIPLE
    }
}
