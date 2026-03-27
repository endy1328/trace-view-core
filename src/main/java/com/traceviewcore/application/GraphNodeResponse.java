package com.traceviewcore.application;

import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.NodeType;
import com.traceviewcore.domain.ReviewStatus;
import java.util.Map;
import java.util.Set;

public record GraphNodeResponse(
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

    public static GraphNodeResponse from(GraphNode node) {
        return new GraphNodeResponse(
                node.id(),
                node.type(),
                node.name(),
                node.sourcePath(),
                node.sourceSymbol(),
                node.confidence(),
                node.reviewStatus(),
                node.tags(),
                node.metadata()
        );
    }
}
