package com.subtitle.service;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Map;

/**
 * Separate Spring bean so calls pass through the cache proxy.
 */
@Service
public class PinyinCharacterCache {

    // pinyin4j outputs breve (ĭ ă ŏ ĕ ŭ) for third tone; standard pinyin uses caron (ǐ ǎ ǒ ě ǔ).
    private static final Map<Character, Character> BREVE_TO_CARON = Map.of(
            '\u012D', '\u01D0', // ĭ → ǐ
            '\u0103', '\u01CE', // ă → ǎ
            '\u014F', '\u01D2', // ŏ → ǒ
            '\u0115', '\u011B', // ĕ → ě
            '\u016D', '\u01D4'  // ŭ → ǔ
    );

    @Cacheable(
            value = "pinyinCache",
            key = "#character + '_' + #toneType + '_' + #caseType + '_' + #vCharType"
    )
    public String convert(char character,
                          HanyuPinyinToneType toneType,
                          HanyuPinyinCaseType caseType,
                          HanyuPinyinVCharType vCharType) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(toneType);
        format.setCaseType(caseType);
        format.setVCharType(vCharType);

        try {
            String[] readings = PinyinHelper.toHanyuPinyinStringArray(character, format);
            String converted = readings != null && readings.length > 0
                    ? readings[0]
                    : String.valueOf(character);
            // pinyin4j may return combining tone marks; normalize to a stable NFC form.
            String nfc = Normalizer.normalize(converted, Normalizer.Form.NFC);
            StringBuilder sb = new StringBuilder(nfc.length());
            for (char c : nfc.toCharArray()) {
                sb.append(BREVE_TO_CARON.getOrDefault(c, c));
            }
            return sb.toString();
        } catch (BadHanyuPinyinOutputFormatCombination ex) {
            return String.valueOf(character);
        }
    }
}
