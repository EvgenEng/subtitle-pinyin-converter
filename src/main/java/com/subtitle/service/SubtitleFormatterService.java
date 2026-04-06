package com.subtitle.service;

import com.subtitle.controller.dto.SubtitleUploadRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Responsible for building final subtitle text output
 * based on selected mode (original / pinyin / dual / triple).
 */
@Slf4j
@Service
public class SubtitleFormatterService {

    /**
     * Formats subtitle text according to selected mode.
     *
     * @param original   original Chinese text
     * @param pinyin     converted pinyin text
     * @param translated translated text (optional, used in TRIPLE mode)
     * @param request    upload request with mode settings
     * @return formatted subtitle text
     */
    public String format(
            String original,
            String pinyin,
            String translated,
            SubtitleUploadRequest request
    ) {

        if (original == null) {
            log.warn("Original text is null");
            return "";
        }

        SubtitleUploadRequest.SubtitleMode mode = request.getMode();

        return switch (mode) {

            case ORIGINAL -> original;

            case PINYIN -> safe(pinyin);

            case DUAL -> buildDual(original, pinyin);

            case TRIPLE -> buildTriple(original, pinyin, translated);
        };
    }

    /**
     * Build DUAL mode:
     * original + pinyin
     */
    private String buildDual(String original, String pinyin) {

        StringBuilder sb = new StringBuilder();

        sb.append(original);

        if (StringUtils.hasText(pinyin)) {
            sb.append("\n").append(pinyin);
        }

        return sb.toString();
    }

    /**
     * Build TRIPLE mode:
     * original + pinyin + translation
     */
    private String buildTriple(String original, String pinyin, String translated) {

        StringBuilder sb = new StringBuilder();

        sb.append(original);

        if (StringUtils.hasText(pinyin)) {
            sb.append("\n").append(pinyin);
        }

        if (StringUtils.hasText(translated)) {
            sb.append("\n").append(translated);
        }

        return sb.toString();
    }

    /**
     * Null-safe string
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }
}
