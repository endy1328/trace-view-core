package com.traceviewcore.application;

import java.util.List;

public record TraceCorrelationLinkResponse(
        String fromRef,
        String toRef,
        String relationType,
        double confidence,
        List<String> eventIds
) {
}
