package com.traceviewcore.application;

import java.time.Instant;

public record AnalysisRunResponse(
        String snapshotId,
        String rootPath,
        Instant createdAt,
        int nodeCount,
        int relationCount,
        int evidenceCount
) {
}
