package com.traceviewcore.application;

import java.time.Instant;
import java.util.List;

public record GraphResponse(
        String snapshotId,
        String rootPath,
        Instant createdAt,
        int nodeCount,
        int relationCount,
        int evidenceCount,
        List<GraphNodeResponse> nodes,
        List<GraphRelationResponse> relations,
        List<EvidenceResponse> evidences
) {
}
