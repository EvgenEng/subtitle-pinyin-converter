package com.subtitle.service;

import com.subtitle.controller.dto.SubtitleUploadRequest;
import com.subtitle.exception.BusinessException;
import com.subtitle.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for converting Chinese text to Pinyin.
 *
 * Provides configurable conversion based on request parameters.
 */
@Slf4j
@Service
public class PinyinConverterService {

    /**
     * Convert Chinese text to Pinyin based on request parameters.
     *
     * @param text    Chinese text to convert
     * @param request Upload request with conversion options
     * @return Pinyin string
     */
    public String convertToPinyin(String text, SubtitleUploadRequest request) {

        if (text == null || text.isBlank()) {
            return text;
        }

        HanyuPinyinOutputFormat format = createFormat(request);
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {

            if (isChinese(c)) {

                String pinyin = convertCharToPinyin(c, format); // ✅ cache используется

                result.append(processPinyin(pinyin, request));

            } else {
                result.append(processNonChinese(c, request));
            }
        }

        return result.toString().trim();
    }

    /**
     * Convert a single Chinese character to Pinyin with caching.
     *
     * Cache key includes character and format configuration.
     *
     * @param ch     Chinese character
     * @param format Output format
     * @return Pinyin representation
     */
    @Cacheable(value = "pinyinCache", key = "#ch + '_' + #format.hashCode()")
    public String convertCharToPinyin(char ch, HanyuPinyinOutputFormat format) {

        try {
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(ch, format);

            if (pinyinArray != null && pinyinArray.length > 0) {
                return pinyinArray[0];
            }

            return String.valueOf(ch);

        } catch (BadHanyuPinyinOutputFormatCombination e) {
            log.warn("Failed to convert character '{}' to Pinyin", ch);
            return String.valueOf(ch);
        }
    }

    /**
     * Create Pinyin output format based on request parameters.
     */
    private HanyuPinyinOutputFormat createFormat(SubtitleUploadRequest request) {

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();

        // Case
        format.setCaseType(
                request.getPinyinFormat() == SubtitleUploadRequest.PinyinFormat.CAPITALIZED
                        ? HanyuPinyinCaseType.LOWERCASE
                        : HanyuPinyinCaseType.LOWERCASE
        );

        // Tone
        format.setToneType(
                request.getPinyinFormat() == SubtitleUploadRequest.PinyinFormat.WITH_TONES
                        ? HanyuPinyinToneType.WITH_TONE_NUMBER
                        : HanyuPinyinToneType.WITHOUT_TONE
        );

        // ü handling
        format.setVCharType(HanyuPinyinVCharType.WITH_U_AND_COLON);

        return format;
    }

    /**
     * Process Pinyin string according to selected format.
     */
    private String processPinyin(String pinyin, SubtitleUploadRequest request) {

        if (pinyin == null || pinyin.isEmpty()) {
            return "";
        }

        return switch (request.getPinyinFormat()) {

            case CAPITALIZED -> capitalizeFirst(pinyin) + " ";

            case WITHOUT_SPACES -> pinyin;

            case WITH_TONES -> pinyin + " ";

            case WITH_SPACES -> pinyin + " ";
        };
    }

    /**
     * Process non-Chinese characters.
     */
    private String processNonChinese(char c, SubtitleUploadRequest request) {

        if (!request.isConvertNonChinese()) {
            return String.valueOf(c);
        }

        if (Character.isWhitespace(c)) {
            return " ";
        }

        return String.valueOf(c);
    }

    /**
     * Check if character is Chinese.
     */
    private boolean isChinese(char c) {
        return Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN;
    }

    /**
     * Capitalize first letter.
     */
    private String capitalizeFirst(String pinyin) {
        return pinyin.substring(0, 1).toUpperCase() + pinyin.substring(1);
    }
}
