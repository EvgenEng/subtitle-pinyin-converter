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
 * Strict parser for SubRip (SRT) subtitle files.
 */
@Slf4j
@Component
public class SrtParser {

    private static final Pattern TIMECODE_PATTERN = Pattern.compile(
            "(\\d{2}:\\d{2}:\\d{2},\\d{3})\\s*-->\\s*(\\d{2}:\\d{2}:\\d{2},\\d{3})"
    );

    public SubtitleFile parse(InputStream inputStream) throws IOException {
        return parse(inputStream, StandardCharsets.UTF_8);
    }

    public SubtitleFile parse(InputStream inputStream, Charset charset) throws IOException {
        return parse(readInputStream(inputStream, charset));
    }

    public SubtitleFile parse(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("Empty SRT content", ErrorCode.EMPTY_FILE);
        }

        // A UTF-8 BOM is sometimes added by Windows editors.
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        if (!normalized.isEmpty() && normalized.charAt(0) == '\uFEFF') {
            normalized = normalized.substring(1);
        }

        String[] rawBlocks = normalized.trim().split("\\n[\\t ]*\\n+");
        List<SubtitleBlock> blocks = new ArrayList<>();

        for (int i = 0; i < rawBlocks.length; i++) {
            String rawBlock = rawBlocks[i].trim();
            if (rawBlock.isEmpty()) {
                continue;
            }
            blocks.add(parseBlock(rawBlock, i + 1));
        }

        if (blocks.isEmpty()) {
            throw new BusinessException("No subtitle blocks found", ErrorCode.INVALID_SRT_FORMAT);
        }

        SubtitleFile subtitleFile = new SubtitleFile();
        subtitleFile.setOriginalContent(content);
        subtitleFile.setBlocks(blocks);
        log.debug("Parsed {} subtitle blocks", blocks.size());
        return subtitleFile;
    }

    private SubtitleBlock parseBlock(String rawBlock, int blockNumber) {
        String[] lines = rawBlock.split("\\n", -1);
        if (lines.length < 3) {
            throw invalidBlock(blockNumber, "expected an index, timecode and subtitle text",
                    ErrorCode.INVALID_SRT_FORMAT);
        }

        final int index;
        try {
            index = Integer.parseInt(lines[0].trim());
        } catch (NumberFormatException ex) {
            throw invalidBlock(blockNumber, "invalid subtitle index: " + lines[0],
                    ErrorCode.MISSING_BLOCK_INDEX);
        }
        if (index <= 0) {
            throw invalidBlock(blockNumber, "subtitle index must be positive",
                    ErrorCode.MISSING_BLOCK_INDEX);
        }

        Matcher matcher = TIMECODE_PATTERN.matcher(lines[1].trim());
        if (!matcher.matches()) {
            throw invalidBlock(blockNumber, "invalid timecode: " + lines[1],
                    ErrorCode.INVALID_TIMECODE);
        }

        String startTime = matcher.group(1);
        String endTime = matcher.group(2);
        final long startMillis;
        final long endMillis;
        try {
            startMillis = toMillis(startTime);
            endMillis = toMillis(endTime);
        } catch (IllegalArgumentException ex) {
            throw invalidBlock(blockNumber, "invalid timecode value: " + ex.getMessage(),
                    ErrorCode.INVALID_TIMECODE);
        }
        if (startMillis >= endMillis) {
            throw invalidBlock(blockNumber,
                    "start time must be before end time (" + startTime + " --> " + endTime + ")",
                    ErrorCode.INVALID_TIMECODE);
        }

        StringBuilder text = new StringBuilder();
        for (int line = 2; line < lines.length; line++) {
            if (line > 2) text.append('\n');
            text.append(lines[line]);
        }
        String subtitleText = text.toString().trim();
        if (subtitleText.isEmpty()) {
            throw invalidBlock(blockNumber, "subtitle text is empty", ErrorCode.INVALID_SRT_FORMAT);
        }

        return SubtitleBlock.builder()
                .index(index)
                .startTime(startTime)
                .endTime(endTime)
                .originalText(subtitleText)
                .build();
    }

    private BusinessException invalidBlock(int blockNumber, String reason, ErrorCode code) {
        return new BusinessException("Invalid SRT block #" + blockNumber + ": " + reason, code);
    }

    public long toMillis(String timecode) {
        String[] parts = timecode.split("[:,]");
        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);
        long seconds = Long.parseLong(parts[2]);
        long millis = Long.parseLong(parts[3]);
        if (minutes > 59 || seconds > 59) {
            throw new IllegalArgumentException("Minutes and seconds must be between 00 and 59");
        }
        return (((hours * 60) + minutes) * 60 + seconds) * 1000 + millis;
    }

    private String readInputStream(InputStream inputStream, Charset charset) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            return content.toString();
        }
    }

    public boolean isValidSrt(String content) {
        try {
            parse(content);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }
}
