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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekTranslationService implements TranslationService {

    @Qualifier("deepSeekWebClient")
    private final WebClient webClient;

    @Override
    public String translateToRussian(String text) {
        try {
            return callDeepSeek(text);
        } catch (Exception e) {
            log.warn("DeepSeek unavailable, fallback used. Reason: {}", e.getMessage());

            // 👉 fallback (не ломаем приложение)
            return "[RU] " + text;
        }
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
}
