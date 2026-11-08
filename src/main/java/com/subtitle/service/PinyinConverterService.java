package com.subtitle.service;

import com.subtitle.controller.dto.SubtitleUploadRequest;
import lombok.RequiredArgsConstructor;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import org.springframework.stereotype.Service;

/**
 * Converts Chinese characters to Pinyin using a dedicated cache component.
 */
@Service
@RequiredArgsConstructor
public class PinyinConverterService {

    private final PinyinCharacterCache characterCache;

    public String convertToPinyin(String text, SubtitleUploadRequest request) {
        if (text == null || text.isBlank()) return text;

        SubtitleUploadRequest.PinyinFormat selected = request.getPinyinFormat();
        HanyuPinyinToneType toneType = selected == SubtitleUploadRequest.PinyinFormat.WITH_TONES
                ? HanyuPinyinToneType.WITH_TONE_MARK
                : HanyuPinyinToneType.WITHOUT_TONE;
        HanyuPinyinCaseType caseType = HanyuPinyinCaseType.LOWERCASE;

        StringBuilder result = new StringBuilder();
        for (char character : text.toCharArray()) {
            if (isChinese(character)) {
                String pinyin = characterCache.convert(
                        character, toneType, caseType, HanyuPinyinVCharType.WITH_U_UNICODE
                );
                if (selected == SubtitleUploadRequest.PinyinFormat.CAPITALIZED) {
                    pinyin = capitalizeFirst(pinyin);
                }
                result.append(pinyin);
                if (selected == SubtitleUploadRequest.PinyinFormat.WITH_SPACES
                        || selected == SubtitleUploadRequest.PinyinFormat.WITH_TONES
                        || selected == SubtitleUploadRequest.PinyinFormat.CAPITALIZED) {
                    result.append(' ');
                }
            } else {
                result.append(processNonChinese(character, request));
            }
        }
        return result.toString().trim();
    }

    private String processNonChinese(char character, SubtitleUploadRequest request) {
        if (!request.isConvertNonChinese()) return String.valueOf(character);
        return Character.isWhitespace(character) ? " " : String.valueOf(character);
    }

    private boolean isChinese(char character) {
        return Character.UnicodeScript.of(character) == Character.UnicodeScript.HAN;
    }

    private String capitalizeFirst(String value) {
        return value.isEmpty() ? value : value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
