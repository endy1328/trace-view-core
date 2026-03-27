package com.traceviewcore.application;

import com.traceviewcore.domain.CertaintyType;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.GraphRelation;
import com.traceviewcore.domain.RelationType;
import java.util.List;

public record GraphRelationResponse(
        String id,
        String fromId,
        String fromName,
        String toId,
        String toName,
        RelationType relationType,
        CertaintyType certaintyType,
        double confidence,
        List<String> evidenceIds
) {

    public static GraphRelationResponse from(GraphRelation relation, GraphNode fromNode, GraphNode toNode) {
        return new GraphRelationResponse(
                relation.id(),
                relation.fromId(),
                fromNode != null ? fromNode.name() : relation.fromId(),
                relation.toId(),
                toNode != null ? toNode.name() : relation.toId(),
                relation.relationType(),
                relation.certaintyType(),
                relation.confidence(),
                relation.evidenceIds()
        );
    }
}
