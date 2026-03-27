package com.traceviewcore.domain;

import java.util.Map;
import java.util.Set;

public record GraphNode(
        String id,
        NodeType type,
        String name,
        String sourcePath,
        String sourceSymbol,
        double confidence,
        ReviewStatus reviewStatus,
        Set<String> tags,
        Map<String, String> metadata
) {
}
