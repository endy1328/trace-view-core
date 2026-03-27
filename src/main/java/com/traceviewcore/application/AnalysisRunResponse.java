package com.traceviewcore.application;

import java.time.Instant;

public record AnalysisRunResponse(
        String snapshotId,
        String rootPath,
        String adapterId,
        Instant createdAt,
        int nodeCount,
        int relationCount,
        int evidenceCount
) {
}
