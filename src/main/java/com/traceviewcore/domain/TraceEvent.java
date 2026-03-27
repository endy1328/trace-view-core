package com.traceviewcore.domain;

import java.time.Instant;
import java.util.Map;

public record TraceEvent(
        String id,
        String sessionId,
        TraceEventType eventType,
        Instant occurredAt,
        String screenName,
        String httpMethod,
        String path,
        String sourceSymbol,
        String traceId,
        String spanId,
        Map<String, String> metadata
) {
}
