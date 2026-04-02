package com.subtitle.parser;

import com.subtitle.exception.BusinessException;
import com.subtitle.parser.model.SubtitleFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

class SrtParserTest {

    private SrtParser srtParser;

    @BeforeEach
    void setUp() {
        srtParser = new SrtParser();
    }

    @Test
    void shouldParseValidSrt() {
        String srtContent = "1\n00:00:01,000 --> 00:00:04,000\nHello World\n\n" +
                "2\n00:00:05,000 --> 00:00:08,000\n你好\n";

        SubtitleFile result = srtParser.parse(srtContent);

        assertThat(result).isNotNull();
        assertThat(result.getBlockCount()).isEqualTo(2);
        assertThat(result.getBlocks().get(0).getOriginalText()).isEqualTo("Hello World");
        assertThat(result.getBlocks().get(1).getOriginalText()).isEqualTo("你好");
    }

    @Test
    void shouldParseMultilineSubtitle() {
        String srtContent = "1\n00:00:01,000 --> 00:00:04,000\nLine 1\nLine 2\n\n";

        SubtitleFile result = srtParser.parse(srtContent);

        assertThat(result.getBlockCount()).isEqualTo(1);
        assertThat(result.getBlocks().get(0).getOriginalText())
                .isEqualTo("Line 1\nLine 2");
    }

    @Test
    void shouldThrowExceptionForEmptyContent() {
        assertThatThrownBy(() -> srtParser.parse(""))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Empty");
    }

    @Test
    void shouldThrowExceptionForInvalidFormat() {
        String invalidContent = "This is not a valid SRT file";

        assertThatThrownBy(() -> srtParser.parse(invalidContent))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldHandleSpecialCharacters() {
        String srtContent = "1\n00:00:01,000 --> 00:00:04,000\n你好，世界！\n\n";

        SubtitleFile result = srtParser.parse(srtContent);

        assertThat(result.getBlocks().get(0).getOriginalText())
                .isEqualTo("你好，世界！");
    }

    @Test
    void shouldValidateSrtCorrectly() {
        String valid = "1\n00:00:01,000 --> 00:00:04,000\nTest\n\n";
        String invalid = "Not valid";

        assertThat(srtParser.isValidSrt(valid)).isTrue();
        assertThat(srtParser.isValidSrt(invalid)).isFalse();
        assertThat(srtParser.isValidSrt(null)).isFalse();
        assertThat(srtParser.isValidSrt("")).isFalse();
    }

    @Test
    void shouldParseFromInputStream() throws Exception {
        String srtContent = "1\n00:00:01,000 --> 00:00:04,000\nTest\n\n";
        InputStream inputStream =
                new ByteArrayInputStream(srtContent.getBytes(StandardCharsets.UTF_8));

        SubtitleFile result = srtParser.parse(inputStream);

        assertThat(result.getBlockCount()).isEqualTo(1);
    }
}