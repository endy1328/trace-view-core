package com.traceviewcore.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModuleClassificationServiceTest {

    private final ModuleClassificationService service = new ModuleClassificationService();

    @TempDir
    Path tempDir;

    @Test
    void classifiesAStoreStyleModules() throws Exception {
        Path web = Files.createDirectories(tempDir.resolve("AStore-ear-backend/AStore-Seller/src/java"));
        Files.createDirectories(tempDir.resolve("AStore-ear-backend/AStore-Seller/WebContent"));
        Files.writeString(web.resolve("SellerController.java"), "@Controller class SellerController {}");

        Path batch = Files.createDirectories(tempDir.resolve("AStore-batch-backend/src/java/config"));
        Files.writeString(batch.resolve("sqlmapBatchConfig.xml"), "<sqlMapConfig></sqlMapConfig>");

        Path lib = Files.createDirectories(tempDir.resolve("AStore-ear-backend/AStore-lib/src/java"));
        Files.writeString(lib.resolve("ProductServiceImpl.java"), "class ProductServiceImpl implements ProductService {}");

        ModuleClassificationResponse response = service.classify(tempDir.toString());

        assertEquals(3, response.moduleCount());
        assertTrue(response.modules().stream().anyMatch(module -> module.name().equals("AStore-Seller") && module.moduleType().equals("WEB_MVC") && module.recommendedAdapterId().equals("astore-web-mvc")));
        assertTrue(response.modules().stream().anyMatch(module -> module.name().equals("AStore-batch-backend") && module.moduleType().equals("BATCH") && module.recommendedAdapterId().equals("astore-batch-legacy")));
        assertTrue(response.modules().stream().anyMatch(module -> module.name().equals("AStore-lib") && module.moduleType().equals("SHARED_LIB") && module.recommendedAdapterId().equals("astore-lib-shared")));
    }
}
