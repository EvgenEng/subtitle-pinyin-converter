package com.subtitle.service;

import com.subtitle.controller.dto.SubtitleUploadRequest;
import org.springframework.stereotype.Service;

/**
 * Responsible for building final subtitle text output
 * based on selected mode (original / pinyin / dual / triple).
 */
@Service
public class SubtitleFormatterService {

    public String format(
            String original,
            String pinyin,
            String translated,
            SubtitleUploadRequest request
    ) {

        return switch (request.getMode()) {

            case ORIGINAL -> original;

            case PINYIN -> pinyin;

            case DUAL -> original + "\n" + pinyin;

            case TRIPLE -> buildTriple(original, pinyin, translated);
        };
    }

    private String buildTriple(String original, String pinyin, String translated) {

        StringBuilder sb = new StringBuilder();

        sb.append(original).append("\n")
                .append(pinyin);

        if (translated != null && !translated.isBlank()) {
            sb.append("\n").append(translated);
        }

        return sb.toString();
    }
}
