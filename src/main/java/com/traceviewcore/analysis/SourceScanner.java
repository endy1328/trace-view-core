package com.traceviewcore.analysis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class SourceScanner {

    public AnalysisContext scan(Path rootPath) throws IOException {
        try (Stream<Path> pathStream = Files.walk(rootPath)) {
            List<SourceDocument> documents = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::toDocument)
                    .toList();
            return new AnalysisContext(rootPath, documents);
        }
    }

    private SourceDocument toDocument(Path path) {
        try {
            return new SourceDocument(path.toString(), Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read source file: " + path, exception);
        }
    }
}
