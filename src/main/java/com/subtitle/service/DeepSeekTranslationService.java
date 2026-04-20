package com.subtitle.service;

import com.subtitle.exception.BusinessException;
import com.subtitle.exception.ErrorCode;
import com.subtitle.service.dto.DeepSeekRequest;
import com.subtitle.service.dto.DeepSeekResponse;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekTranslationService implements TranslationService {

    @Qualifier("deepSeekWebClient")
    private final WebClient webClient;

    private final CacheManager cacheManager;

    private Cache getCache() {
        return cacheManager.getCache("translationCache");
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_DELAY_MS = 500;

    @Override
    public String translateToRussian(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        Cache cache = getCache();

        String cached = cache.get(text, String.class);
        if (cached != null) {
            return cached;
        }

        try {
            String result = callDeepSeek(text);
            cache.put(text, result);
            return result;
        } catch (Exception e) {
            log.warn("Fallback used for text: {}", text);
            return "[RU] " + text;
        }
    }

    @Override
    public List<String> translateBatch(List<String> texts) {

        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        int CHUNK_SIZE = 5;
        List<List<String>> chunks = new ArrayList<>();

        for (int i = 0; i < texts.size(); i += CHUNK_SIZE) {
            chunks.add(texts.subList(i, Math.min(i + CHUNK_SIZE, texts.size())));
        }

        List<CompletableFuture<List<String>>> futures = new ArrayList<>();

        for (List<String> chunk : chunks) {
            futures.add(
                    CompletableFuture.supplyAsync(() -> processChunk(chunk), executor)
            );
        }

        List<String> result = new ArrayList<>();

        for (CompletableFuture<List<String>> future : futures) {
            result.addAll(future.join());
        }

        return result;
    }

    private List<String> processChunk(List<String> chunk) {

        log.info("Processing chunk in thread: {}", Thread.currentThread().getName());

        Cache cache = getCache();

        List<String> result = new ArrayList<>();

        for (String text : chunk) {

            if (text == null || text.isBlank()) {
                result.add("");
                continue;
            }

            String cached = cache.get(text, String.class);

            if (cached != null) {
                result.add(cached);
                continue;
            }

            try {
                String translated = callDeepSeek(text);
                cache.put(text, translated);
                result.add(translated);
            } catch (Exception e) {
                log.warn("Fallback used for text: {}", text);
                result.add("[RU] " + text);
            }
        }

        return result;
    }

    private String callDeepSeek(String text) {

        long delay = INITIAL_DELAY_MS;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {

                DeepSeekRequest request = DeepSeekRequest.builder()
                        .model("deepseek-chat")
                        .messages(List.of(
                                DeepSeekRequest.Message.builder()
                                        .role("system")
                                        .content("Translate Chinese text to Russian. Keep line breaks.")
                                        .build(),
                                DeepSeekRequest.Message.builder()
                                        .role("user")
                                        .content(text)
                                        .build()
                        ))
                        .build();

                DeepSeekResponse response = webClient.post()
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(DeepSeekResponse.class)
                        .block();

                if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                    throw new BusinessException("Empty response from DeepSeek", ErrorCode.SERVICE_UNAVAILABLE);
                }

                return response.getChoices()
                        .get(0)
                        .getMessage()
                        .getContent()
                        .trim();

            } catch (Exception e) {

                log.warn("DeepSeek attempt {} failed: {}", attempt, e.getMessage());

                if (attempt == MAX_ATTEMPTS) {
                    throw e;
                }

                sleep(delay);
                delay *= 2;
            }
        }

        throw new IllegalStateException("Unexpected retry failure");
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry interrupted", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down DeepSeek executor");
        executor.shutdown();
    }
}
