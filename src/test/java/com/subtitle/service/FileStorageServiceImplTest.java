package com.subtitle.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateDifferentOutputNamesForSameOriginalFile() throws Exception {
        FileStorageServiceImpl storage = new FileStorageServiceImpl();
        Path converted = tempDir.resolve("converted");
        ReflectionTestUtils.setField(storage, "convertedPath", converted.toString());

        String first = storage.saveConvertedFile("first", "lesson.srt");
        String second = storage.saveConvertedFile("second", "lesson.srt");

        assertThat(first).isNotEqualTo(second);
        assertThat(first).matches("lesson_[a-f0-9]{8}_pinyin\\.srt");
        assertThat(second).matches("lesson_[a-f0-9]{8}_pinyin\\.srt");
        assertThat(Files.readString(converted.resolve(first))).isEqualTo("first");
        assertThat(Files.readString(converted.resolve(second))).isEqualTo("second");
    }
}
