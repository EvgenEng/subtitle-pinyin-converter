package com.subtitle.service;

import com.subtitle.controller.dto.SubtitleResponse;
import com.subtitle.controller.dto.SubtitleUploadRequest;
import com.subtitle.exception.BusinessException;
import com.subtitle.exception.ErrorCode;
import com.subtitle.parser.SrtParser;
import com.subtitle.parser.model.SubtitleBlock;
import com.subtitle.parser.model.SubtitleFile;
import com.subtitle.util.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Implementation of SubtitleService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubtitleServiceImpl implements SubtitleService {

    private final SrtParser srtParser;
    private final PinyinConverterService pinyinConverter;
    private final FileValidator fileValidator;
    private final FileStorageService fileStorageService;
    private final SubtitleFormatterService formatter;
    private final TranslationService translationService;

    @Override
    @Transactional
    public SubtitleResponse convertSrtToPinyin(MultipartFile file, SubtitleUploadRequest request) {

        log.info("Starting subtitle conversion. File: {}, mode: {}",
                file.getOriginalFilename(),
                request.getMode());

        try {
            // 1. Validation
            fileValidator.validate(file);

            // 2. Read file
            byte[] bytes = file.getBytes();

            // 3. Parse SRT
            SubtitleFile subtitleFile = srtParser.parse(
                    new ByteArrayInputStream(bytes)
            );

            log.debug("Parsed {} subtitle blocks", subtitleFile.getBlockCount());

            // 4. Process each block
            for (SubtitleBlock block : subtitleFile.getBlocks()) {

                String original = block.getOriginalText();

                String pinyin = pinyinConverter.convertToPinyin(
                        original,
                        request
                );

                String translated = null;

                if (request.getMode() == SubtitleUploadRequest.SubtitleMode.TRIPLE) {
                    translated = translationService.translateToRussian(original);
                }

                String finalText = formatter.format(
                        original,
                        pinyin,
                        translated,
                        request
                );

                block.setConvertedText(finalText);
            }

            // 5. Build result content
            String content = subtitleFile.buildSrtContent();

            // 6. Save file
            String fileName = fileStorageService.saveConvertedFile(
                    content,
                    file.getOriginalFilename()
            );

            log.info("File successfully converted: {}", fileName);

            // 7. Build response
            return SubtitleResponse.builder()
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .convertedContent(content)
                    .originalSize(file.getSize())
                    .convertedSize(content.getBytes(StandardCharsets.UTF_8).length)
                    .blockCount(subtitleFile.getBlockCount())
                    .conversionTime(LocalDateTime.now())
                    .downloadUrl("/api/v1/subtitle/download/" + fileName)
                    .build();

        } catch (IOException e) {
            log.error("File processing failed", e);

            throw new BusinessException(
                    "Failed to process file",
                    ErrorCode.FILE_READ_ERROR,
                    e
            );
        }
    }

    @Override
    public void validateSrtFile(MultipartFile file) {
        try {
            fileValidator.validate(file);
        } catch (IOException e) {
            throw new BusinessException(
                    "Failed to validate file",
                    ErrorCode.FILE_READ_ERROR,
                    e
            );
        }
    }

    @Override
    public String getFileStatistics(String fileName) {
        return "Not implemented yet";
    }

    @Override
    public ResponseEntity<Resource> download(String fileName) {

        log.info("Downloading file: {}", fileName);

        Resource resource = fileStorageService.loadFile(fileName);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
