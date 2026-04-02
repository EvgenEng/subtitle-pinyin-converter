package com.subtitle.service;

import com.subtitle.controller.dto.SubtitleUploadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PinyinConverterServiceTest {

    private PinyinConverterService service;

    @BeforeEach
    void setUp() {
        service = new PinyinConverterService();
    }

    @Test
    void shouldConvertChineseToPinyin() {
        String chinese = "你好";

        SubtitleUploadRequest request = SubtitleUploadRequest.builder()
                .withToneMarks(false)
                .pinyinFormat(SubtitleUploadRequest.PinyinFormat.WITH_SPACES)
                .build();

        String result = service.convertToPinyin(chinese, request);

        assertThat(result).isEqualTo("ni hao"); // 🔥 без лишнего пробела
    }

    @Test
    void shouldConvertWithTones() {
        String chinese = "你好";

        SubtitleUploadRequest request = SubtitleUploadRequest.builder()
                .pinyinFormat(SubtitleUploadRequest.PinyinFormat.WITH_TONES)
                .build();

        String result = service.convertToPinyin(chinese, request);

        assertThat(result)
                .satisfiesAnyOf(
                        r -> assertThat(r).contains("ni3"),
                        r -> assertThat(r).contains("nǐ")
                );
    }

    @Test
    void shouldConvertCapitalized() {
        String chinese = "你好";

        SubtitleUploadRequest request = SubtitleUploadRequest.builder()
                .pinyinFormat(SubtitleUploadRequest.PinyinFormat.CAPITALIZED)
                .build();

        String result = service.convertToPinyin(chinese, request);

        assertThat(result).isEqualTo("Ni Hao");
    }

    @Test
    void shouldConvertWithoutSpaces() {
        String chinese = "你好";

        SubtitleUploadRequest request = SubtitleUploadRequest.builder()
                .pinyinFormat(SubtitleUploadRequest.PinyinFormat.WITHOUT_SPACES)
                .build();

        String result = service.convertToPinyin(chinese, request);

        assertThat(result).isEqualTo("nihao");
    }

    @Test
    void shouldHandleMixedContent() {
        String mixed = "Hello 你好 World";

        SubtitleUploadRequest request = SubtitleUploadRequest.builder()
                .convertNonChinese(true)
                .pinyinFormat(SubtitleUploadRequest.PinyinFormat.WITH_SPACES)
                .build();

        String result = service.convertToPinyin(mixed, request);

        assertThat(result).contains("ni hao");
        assertThat(result).contains("Hello");
        assertThat(result).contains("World");
    }

    @Test
    void shouldReturnEmptyForEmptyInput() {
        String result = service.convertToPinyin("", SubtitleUploadRequest.builder().build());

        assertThat(result).isEmpty();
    }
}
