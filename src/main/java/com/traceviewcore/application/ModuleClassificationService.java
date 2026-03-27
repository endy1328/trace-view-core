package com.traceviewcore.application;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ModuleClassificationService {

    public ModuleClassificationResponse classify(String rootPath) throws IOException {
        Path root = Path.of(rootPath);
        if (!Files.exists(root)) {
            throw new IllegalArgumentException("Source root path not found: " + rootPath);
        }
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Source root path is not a directory: " + rootPath);
        }

        List<ModuleDescriptorResponse> modules = discoverModules(root).stream()
                .sorted(Comparator.comparing(ModuleDescriptorResponse::path))
                .toList();

        return new ModuleClassificationResponse(
                root.toString(),
                Instant.now(),
                modules.size(),
                modules
        );
    }

    private List<ModuleDescriptorResponse> discoverModules(Path root) throws IOException {
        List<ModuleDescriptorResponse> modules = new ArrayList<>();
        try (DirectoryStream<Path> firstLevel = Files.newDirectoryStream(root)) {
            for (Path child : firstLevel) {
                if (!Files.isDirectory(child) || ignoreDir(child)) {
                    continue;
                }
                List<Path> nestedModules = nestedModules(child);
                if (!nestedModules.isEmpty()) {
                    for (Path nested : nestedModules) {
                        modules.add(describeModule(nested));
                    }
                } else if (looksLikeModule(child)) {
                    modules.add(describeModule(child));
                }
            }
        }
        return modules;
    }

    private List<Path> nestedModules(Path parent) throws IOException {
        List<Path> nestedModules = new ArrayList<>();
        try (DirectoryStream<Path> secondLevel = Files.newDirectoryStream(parent)) {
            for (Path nested : secondLevel) {
                if (!Files.isDirectory(nested) || ignoreDir(nested) || !looksLikeModule(nested)) {
                    continue;
                }
                nestedModules.add(nested);
            }
        }
        return nestedModules;
    }

    private boolean ignoreDir(Path path) {
        String name = path.getFileName().toString();
        return name.startsWith(".")
                || name.equals("lib")
                || name.equals("EarContent")
                || name.equals("bin")
                || name.equals("files-for-eclipse-manual-import")
                || name.equals("deployment-manager");
    }

    private boolean looksLikeModule(Path path) {
        return Files.isDirectory(path.resolve("src"))
                || Files.isDirectory(path.resolve("WebContent"))
                || Files.exists(path.resolve("build.gradle"))
                || Files.exists(path.resolve("build.xml"));
    }

    private ModuleDescriptorResponse describeModule(Path modulePath) throws IOException {
        List<String> reasons = new ArrayList<>();
        String name = modulePath.getFileName().toString();

        boolean hasWebContent = Files.isDirectory(modulePath.resolve("WebContent"));
        boolean hasJavaSrc = Files.isDirectory(modulePath.resolve("src/java"));
        boolean hasTestSrc = Files.isDirectory(modulePath.resolve("src/test"));
        boolean hasMapper = containsGlob(modulePath, "**/*Mapper.xml");
        boolean hasSqlmap = containsGlob(modulePath, "**/sqlmap*.xml");
        boolean hasController = containsJavaToken(modulePath, "@Controller") || containsJavaToken(modulePath, "@RestController");
        boolean hasBatchHint = name.toLowerCase().contains("batch")
                || containsJavaToken(modulePath, "Job")
                || containsJavaToken(modulePath, "Batch")
                || hasSqlmap;
        boolean hasSharedHint = name.toLowerCase().contains("lib");

        String moduleType;
        String adapterId;

        if (hasWebContent || hasController) {
            moduleType = "WEB_MVC";
            adapterId = "astore-web-mvc";
            reasons.add("controller or WebContent detected");
        } else if (hasSharedHint) {
            moduleType = "SHARED_LIB";
            adapterId = "astore-lib-shared";
            reasons.add("shared library module naming detected");
        } else if (hasBatchHint) {
            moduleType = "BATCH";
            adapterId = "astore-batch-legacy";
            reasons.add("batch naming or sqlmap configuration detected");
        } else if (hasMapper || hasJavaSrc) {
            moduleType = "SHARED_LIB";
            adapterId = "astore-lib-shared";
            reasons.add("shared library style source layout detected");
        } else {
            moduleType = "UNKNOWN";
            adapterId = "astore-legacy";
            reasons.add("fallback to common legacy adapter");
        }

        if (hasMapper) {
            reasons.add("mapper xml present");
        }
        if (hasSqlmap) {
            reasons.add("sqlmap xml present");
        }
        if (hasTestSrc) {
            reasons.add("test sources present");
        }

        return new ModuleDescriptorResponse(
                name,
                modulePath.toString(),
                moduleType,
                adapterId,
                reasons
        );
    }

    private boolean containsJavaToken(Path root, String token) throws IOException {
        try (var paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .limit(4000)
                    .anyMatch(path -> containsToken(path, token));
        }
    }

    private boolean containsGlob(Path root, String pattern) throws IOException {
        var matcher = root.getFileSystem().getPathMatcher("glob:" + pattern);
        try (var paths = Files.walk(root)) {
            return paths.anyMatch(path -> matcher.matches(root.relativize(path)));
        }
    }

    private boolean containsToken(Path path, String token) {
        try {
            return Files.readString(path).contains(token);
        } catch (IOException exception) {
            return false;
        }
    }
}
