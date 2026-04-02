package com.subtitle.parser;

import com.subtitle.exception.BusinessException;
import com.subtitle.exception.ErrorCode;
import com.subtitle.parser.model.SubtitleBlock;
import com.subtitle.parser.model.SubtitleFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for SRT (SubRip Text) subtitle files.
 */
@Slf4j
@Component
public class SrtParser {

    private static final Pattern TIMECODE_PATTERN = Pattern.compile(
            "(\\d{2}:\\d{2}:\\d{2},\\d{3}) --> (\\d{2}:\\d{2}:\\d{2},\\d{3})"
    );

    /**
     * Parse SRT content from InputStream.
     */
    public SubtitleFile parse(InputStream inputStream) throws IOException {
        return parse(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * Parse SRT content from InputStream with charset.
     */
    public SubtitleFile parse(InputStream inputStream, Charset charset) throws IOException {
        String content = readInputStream(inputStream, charset);
        return parse(content);
    }

    /**
     * стабильный парсинг без regex-ада
     */
    public SubtitleFile parse(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("Empty SRT content", ErrorCode.EMPTY_FILE);
        }

        SubtitleFile subtitleFile = new SubtitleFile();
        List<SubtitleBlock> blocks = new ArrayList<>();

        // делим файл на блоки по пустой строке
        String[] rawBlocks = content.split("\\n\\s*\\n");

        for (String rawBlock : rawBlocks) {
            try {
                String[] lines = rawBlock.split("\\n");

                if (lines.length < 3) continue;

                int index = Integer.parseInt(lines[0].trim());

                Matcher timeMatcher = TIMECODE_PATTERN.matcher(lines[1].trim());
                if (!timeMatcher.matches()) {
                    throw new BusinessException(
                            "Invalid timecode format: " + lines[1],
                            ErrorCode.INVALID_TIMECODE
                    );
                }

                String startTime = timeMatcher.group(1);
                String endTime = timeMatcher.group(2);

                // собираем ВЕСЬ текст (включая много строк)
                StringBuilder textBuilder = new StringBuilder();
                for (int i = 2; i < lines.length; i++) {
                    textBuilder.append(lines[i]);
                    if (i < lines.length - 1) {
                        textBuilder.append("\n");
                    }
                }

                SubtitleBlock block = SubtitleBlock.builder()
                        .index(index)
                        .startTime(startTime)
                        .endTime(endTime)
                        .originalText(textBuilder.toString().trim())
                        .build();

                blocks.add(block);

            } catch (Exception e) {
                log.warn("Failed to parse block: {}", rawBlock, e);
            }
        }

        if (blocks.isEmpty()) {
            throw new BusinessException("No valid subtitle blocks found", ErrorCode.INVALID_SRT_FORMAT);
        }

        subtitleFile.setOriginalContent(content);
        subtitleFile.setBlocks(blocks);

        log.debug("Parsed {} blocks", blocks.size());

        return subtitleFile;
    }

    /**
     * Read InputStream to String
     */
    private String readInputStream(InputStream inputStream, Charset charset) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * Validate SRT
     */
    public boolean isValidSrt(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        String[] rawBlocks = content.split("\\n\\s*\\n");

        for (String rawBlock : rawBlocks) {
            String[] lines = rawBlock.split("\\n");
            if (lines.length >= 3) {
                Matcher matcher = TIMECODE_PATTERN.matcher(lines[1].trim());
                if (matcher.matches()) {
                    return true;
                }
            }
        }

        return false;
    }
}
