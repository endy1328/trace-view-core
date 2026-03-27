package com.traceviewcore.domain;

import java.time.Instant;

public record AnalysisSnapshot(
        String id,
        String rootPath,
        Instant createdAt,
        AnalysisGraph graph
) {
}
