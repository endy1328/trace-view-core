package com.traceviewcore.domain;

import java.util.List;

public record AnalysisGraph(
        List<GraphNode> nodes,
        List<GraphRelation> relations,
        List<Evidence> evidences
) {
}
