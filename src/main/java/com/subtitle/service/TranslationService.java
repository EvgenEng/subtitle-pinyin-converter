package com.subtitle.service;

import java.util.List;

/**
 * Service for translating subtitle text.
 */
public interface TranslationService {

    /**
     * Translate Chinese text to Russian.
     *
     * @param text original text
     * @return translated text
     */
    String translateToRussian(String text);

    List<String> translateBatch(List<String> texts);
}
