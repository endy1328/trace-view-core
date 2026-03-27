package com.traceviewcore.application;

import com.traceviewcore.domain.NodeType;

public record TraceCorrelationNodeResponse(
        String eventId,
        String nodeId,
        NodeType nodeType,
        String nodeName,
        String matchReason
) {
}
