package com.subtitle.service;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Mock implementation of TranslationService.
 */
@Slf4j
public class MockTranslationService implements TranslationService {

    @Override
    public String translateToRussian(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        log.debug("Mock translating: {}", text);

        return "[RU] " + text;
    }

    @Override
    public List<String> translateBatch(List<String> texts) {
        return texts.stream()
                .map(text -> "[RU] " + text)
                .toList();
    }
}
