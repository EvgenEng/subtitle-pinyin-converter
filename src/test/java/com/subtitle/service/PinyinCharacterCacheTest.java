package com.subtitle.service;

import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(PinyinCharacterCacheTest.CacheTestConfiguration.class)
class PinyinCharacterCacheTest {

    @Autowired
    private PinyinCharacterCache characterCache;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldStoreConvertedCharacterInSpringCache() {
        String result = characterCache.convert(
                '你',
                HanyuPinyinToneType.WITH_TONE_MARK,
                HanyuPinyinCaseType.LOWERCASE,
                HanyuPinyinVCharType.WITH_U_UNICODE
        );

        assertThat(result).isEqualTo("n\u01D0");
        assertThat(cacheManager.getCache("pinyinCache")).isNotNull();
        assertThat(cacheManager.getCache("pinyinCache").get(
                "你_WITH_TONE_MARK_LOWERCASE_WITH_U_UNICODE"
        )).isNotNull();
    }

    @Configuration
    @EnableCaching
    @Import(PinyinCharacterCache.class)
    static class CacheTestConfiguration {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("pinyinCache");
        }
    }
}
