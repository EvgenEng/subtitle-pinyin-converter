package com.subtitle.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO for subtitle file upload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleUploadRequest {

    /**
     * Uploaded subtitle file
     */
    @NotNull(message = "File cannot be null")
    private MultipartFile file;

    /**
     * Use tone marks in pinyin
     */
    @Builder.Default
    private boolean withToneMarks = true;

    /**
     * Pinyin formatting option
     */
    @Builder.Default
    private PinyinFormat pinyinFormat = PinyinFormat.WITH_SPACES;

    /**
     * Convert non-Chinese characters
     */
    @Builder.Default
    private boolean convertNonChinese = false;

    /**
     * Subtitle output mode
     */
    @Builder.Default
    private SubtitleMode mode = SubtitleMode.PINYIN;

    /**
     * Pinyin format options
     */
    public enum PinyinFormat {
        WITH_SPACES,
        WITHOUT_SPACES,
        WITH_TONES,
        CAPITALIZED
    }

    /**
     * Subtitle output modes
     */
    public enum SubtitleMode {
        ORIGINAL, // только оригинал
        PINYIN,   // только пиньинь (текущий default)
        DUAL,     // оригинал + пиньинь
        TRIPLE    // оригинал + пиньинь + перевод (будет через DeepSeek)
    }
}
