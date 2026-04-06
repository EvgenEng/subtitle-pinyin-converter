package com.subtitle.service;

import com.subtitle.service.TranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock implementation of TranslationService.
 * Will be replaced with DeepSeek integration later.
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
}
