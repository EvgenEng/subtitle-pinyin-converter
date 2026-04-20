package com.subtitle.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${subtitle.upload.path}")
    private String uploadPath;

    @Value("${subtitle.converted.path}")
    private String convertedPath;

    @Override
    public String saveTempFile(MultipartFile file) throws IOException {

        Path dir = Paths.get(uploadPath);
        Files.createDirectories(dir);

        String originalName = sanitizeFilename(file.getOriginalFilename());
        String name = UUID.randomUUID() + "_" + originalName;

        Path path = dir.resolve(name);
        Files.write(path, file.getBytes());

        return name;
    }

    @Override
    public String saveConvertedFile(String content, String originalFileName) throws IOException {

        Path dir = Paths.get(convertedPath);
        Files.createDirectories(dir);

        String safeName = sanitizeFilename(originalFileName);

        int dotIndex = safeName.lastIndexOf('.');
        String baseName = (dotIndex != -1)
                ? safeName.substring(0, dotIndex)
                : safeName;

        String fileName = baseName + "_pinyin.srt";

        Path path = dir.resolve(fileName);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));

        return fileName;
    }

    @Override
    public Resource loadFile(String fileName) {
        try {

            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid filename");
            }

            Path path = Paths.get(convertedPath)
                    .resolve(fileName)
                    .normalize();

            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found");
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new RuntimeException("Error loading file", e);
        }
    }

    @Override
    public void deleteTempFile(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(uploadPath, fileName));
        } catch (IOException e) {
            log.warn("Failed to delete temp file {}", fileName);
        }
    }

    private String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "file.srt";
        }

        return filename
                .replaceAll("[^a-zA-Z0-9\\.\\-]", "_")
                .replace("..", "_");
    }
}
