package com.subtitle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main application class for Subtitle Pinyin Converter.
 * Converts Chinese SRT subtitles to Pinyin format.
 *
 */
@SpringBootApplication
@EnableCaching
public class SubtitlePinyinApplication {

    /**
     * Entry point for the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SubtitlePinyinApplication.class, args);
    }
}
