package com.subtitle.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.api-key=test-api-key")
class SubtitleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldConvertSrtFileSuccessfully() throws Exception {

        String srtContent = """
                1
                00:00:01,000 --> 00:00:03,000
                你好

                2
                00:00:04,000 --> 00:00:06,000
                世界
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.srt",
                MediaType.TEXT_PLAIN_VALUE,
                srtContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/subtitle/convert")
                        .file(file)
                        .header("X-API-KEY", "test-api-key")
                        .param("pinyinFormat", "WITH_SPACES")
                        .param("convertNonChinese", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").exists())
                .andExpect(jsonPath("$.data.blockCount").value(2));
    }

    @Test
    void shouldFailWhenFileIsEmpty() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.srt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/subtitle/convert")
                        .file(file)
                        .header("X-API-KEY", "test-api-key"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldFailWhenInvalidFileType() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "invalid content".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/subtitle/convert")
                        .file(file)
                        .header("X-API-KEY", "test-api-key"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldDownloadFileSuccessfully() throws Exception {

        String srtContent = """
            1
            00:00:01,000 --> 00:00:03,000
            你好
            """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.srt",
                MediaType.TEXT_PLAIN_VALUE,
                srtContent.getBytes(StandardCharsets.UTF_8)
        );

        // Upload first and read the generated filename (it contains a unique suffix).
        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/subtitle/convert")
                        .file(file)
                        .header("X-API-KEY", "test-api-key")
                        .param("pinyinFormat", "WITH_SPACES")
                        .param("convertNonChinese", "false"))
                .andExpect(status().isOk())
                .andReturn();

        String generatedFileName = JsonPath.read(
                uploadResult.getResponse().getContentAsString(), "$.data.fileName");

        // Download the exact file returned by the conversion endpoint.
        mockMvc.perform(get("/api/v1/subtitle/download/{fileName}", generatedFileName)
                        .header("X-API-KEY", "test-api-key"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")));
    }
}
