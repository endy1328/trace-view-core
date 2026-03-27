package com.traceviewcore.analysis;

import java.nio.file.Path;
import java.util.List;

public record AnalysisContext(
        Path rootPath,
        List<SourceDocument> sourceDocuments
) {
}
