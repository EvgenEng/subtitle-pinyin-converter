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

/**
 * Separate Spring bean so calls pass through the cache proxy.
 */
@Service
public class PinyinCharacterCache {

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
            return Normalizer.normalize(converted, Normalizer.Form.NFC);
        } catch (BadHanyuPinyinOutputFormatCombination ex) {
            return String.valueOf(character);
        }
    }
}
