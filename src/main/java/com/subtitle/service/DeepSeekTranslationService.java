package com.subtitle.service;

import com.subtitle.exception.BusinessException;
import com.subtitle.exception.ErrorCode;
import com.subtitle.service.dto.DeepSeekRequest;
import com.subtitle.service.dto.DeepSeekResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekTranslationService implements TranslationService {

    @Qualifier("deepSeekWebClient")
    private final WebClient webClient;

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_DELAY_MS = 500;

    // ================================
    // SINGLE TRANSLATION
    // ================================
    @Override
    public String translateToRussian(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        if (cache.containsKey(text)) {
            log.debug("Cache hit for text: {}", text);
            return cache.get(text);
        }

        try {
            String translated = callDeepSeek(text);
            cache.put(text, translated);
            return translated;

        } catch (Exception e) {
            log.warn("DeepSeek unavailable, fallback used. Reason: {}", e.getMessage());

            String fallback = "[RU] " + text;
            cache.put(text, fallback);

            return fallback;
        }
    }

    // ================================
    // BATCH TRANSLATION
    // ================================
    @Override
    public List<String> translateBatch(List<String> texts) {

        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        try {
            return callDeepSeekBatch(texts);
        } catch (Exception e) {
            log.warn("Batch translation failed, fallback used");

            return texts.stream()
                    .map(t -> {
                        String fallback = "[RU] " + t;
                        cache.put(t, fallback);
                        return fallback;
                    })
                    .toList();
        }
    }

    // ================================
    // RETRY + BACKOFF
    // ================================
    private String callDeepSeek(String text) {

        long delay = INITIAL_DELAY_MS;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                log.debug("DeepSeek attempt {}", attempt);

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

    // ================================
    // ASYNC BATCH (С ОГРАНИЧЕНИЕМ ПОТОКОВ)
    // ================================
    private List<String> callDeepSeekBatch(List<String> texts) {

        int CHUNK_SIZE = 5;

        List<List<String>> chunks = new ArrayList<>();

        for (int i = 0; i < texts.size(); i += CHUNK_SIZE) {
            chunks.add(texts.subList(i, Math.min(i + CHUNK_SIZE, texts.size())));
        }

        List<CompletableFuture<List<String>>> futures = new ArrayList<>();

        for (List<String> chunk : chunks) {

            CompletableFuture<List<String>> future =
                    CompletableFuture.supplyAsync(() -> processChunk(chunk), executor);

            futures.add(future);
        }

        List<String> result = new ArrayList<>();

        for (CompletableFuture<List<String>> future : futures) {
            result.addAll(future.join());
        }

        return result;
    }

    // ================================
    // PROCESS CHUNK
    // ================================
    private List<String> processChunk(List<String> chunk) {

        log.info("Processing chunk in thread: {}", Thread.currentThread().getName());

        List<String> result = new ArrayList<>();

        for (String text : chunk) {

            if (text == null || text.isBlank()) {
                result.add("");
                continue;
            }

            if (cache.containsKey(text)) {
                log.debug("Cache hit for text: {}", text);
                result.add(cache.get(text));
                continue;
            }

            try {
                String translated = callDeepSeek(text);

                cache.put(text, translated);

                result.add(translated);

            } catch (Exception e) {
                log.warn("Chunk item failed, fallback used");

                String fallback = "[RU] " + text;

                cache.put(text, fallback);

                result.add(fallback);
            }
        }

        return result;
    }

    // ================================
    // UTIL
    // ================================
    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry interrupted", e);
        }
    }
}
