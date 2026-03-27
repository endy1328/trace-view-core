package com.traceviewcore.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.traceviewcore.bootstrap.TraceViewCoreApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TraceViewCoreApplication.class)
@AutoConfigureMockMvc
class ModuleClassificationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    @Test
    void classifiesModulesAndRecommendsAdapters() throws Exception {
        Path web = Files.createDirectories(tempDir.resolve("AStore-ear-backend/AStore-Seller/src/java"));
        Files.createDirectories(tempDir.resolve("AStore-ear-backend/AStore-Seller/WebContent"));
        Files.writeString(web.resolve("SellerController.java"), "@Controller class SellerController {}");

        Path batch = Files.createDirectories(tempDir.resolve("AStore-batch-backend/src/java/config"));
        Files.writeString(batch.resolve("sqlmapBatchConfig.xml"), "<sqlMapConfig></sqlMapConfig>");

        mockMvc.perform(post("/api/analysis/classify-modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rootPath":"%s"}
                                """.formatted(tempDir.toString().replace("\\", "\\\\"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moduleCount", is(2)))
                .andExpect(jsonPath("$.modules", hasSize(2)))
                .andExpect(jsonPath("$.modules[0].recommendedAdapterId").exists());
    }
}
