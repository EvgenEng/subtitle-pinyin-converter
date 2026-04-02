package com.subtitle.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    String saveTempFile(MultipartFile file) throws IOException;

    String saveConvertedFile(String content, String originalFileName) throws IOException;

    Resource loadFile(String fileName);

    void deleteTempFile(String fileName);
}
