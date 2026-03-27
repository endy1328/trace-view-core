package com.traceviewcore.application;

import java.util.List;

public record NodeDetailResponse(
        GraphNodeResponse node,
        List<ConnectedNodeRelationResponse> incomingRelations,
        List<ConnectedNodeRelationResponse> outgoingRelations,
        List<EvidenceResponse> evidences,
        List<AnnotationResponse> annotations
) {
}
