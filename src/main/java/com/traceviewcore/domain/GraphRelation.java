package com.traceviewcore.domain;

import java.util.List;

public record GraphRelation(
        String id,
        String fromId,
        String toId,
        RelationType relationType,
        CertaintyType certaintyType,
        double confidence,
        List<String> evidenceIds
) {
}
