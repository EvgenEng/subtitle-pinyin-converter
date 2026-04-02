package com.subtitle.service;

import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    /**
     * Temporary translation stub.
     *
     * @param text Chinese text
     * @return placeholder translation
     */
    public String translateToRussian(String text) {
        return "[RU translation pending] " + text;
    }
}
