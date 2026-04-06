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

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekTranslationService implements TranslationService {

    private static final int BATCH_SIZE = 20;

    @Qualifier("deepSeekWebClient")
    private final WebClient webClient;

    @Override
    public String translateToRussian(String text) {
        try {
            return callDeepSeek(text);
        } catch (Exception e) {
            log.warn("DeepSeek unavailable, fallback used. Reason: {}", e.getMessage());
            return "[RU] " + text;
        }
    }

    @Override
    public List<String> translateBatch(List<String> texts) {

        List<String> result = new ArrayList<>();

        for (int i = 0; i < texts.size(); i += BATCH_SIZE) {

            List<String> chunk = texts.subList(i, Math.min(i + BATCH_SIZE, texts.size()));

            try {
                result.addAll(callDeepSeekBatch(chunk));
            } catch (Exception e) {
                log.warn("Chunk failed, using fallback. Reason: {}", e.getMessage());

                for (String text : chunk) {
                    result.add("[RU] " + text);
                }
            }
        }

        return result;
    }

    private String callDeepSeek(String text) {

        DeepSeekRequest request = DeepSeekRequest.builder()
                .model("deepseek-chat")
                .messages(List.of(
                        DeepSeekRequest.Message.builder()
                                .role("system")
                                .content("Translate Chinese text to Russian")
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
    }

    private List<String> callDeepSeekBatch(List<String> texts) {

        String joinedText = String.join("\n", texts);

        DeepSeekRequest request = DeepSeekRequest.builder()
                .model("deepseek-chat")
                .messages(List.of(
                        DeepSeekRequest.Message.builder()
                                .role("system")
                                .content("Translate each line from Chinese to Russian. Return SAME number of lines.")
                                .build(),
                        DeepSeekRequest.Message.builder()
                                .role("user")
                                .content(joinedText)
                                .build()
                ))
                .build();

        DeepSeekResponse response = webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DeepSeekResponse.class)
                .block();

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new BusinessException("Empty batch response", ErrorCode.SERVICE_UNAVAILABLE);
        }

        String content = response.getChoices()
                .get(0)
                .getMessage()
                .getContent();

        return List.of(content.split("\n"));
    }
}
