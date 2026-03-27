package com.traceviewcore.application;

import com.traceviewcore.domain.TraceEvent;
import com.traceviewcore.domain.TraceEventType;
import java.time.Instant;
import java.util.Map;

public record TraceEventResponse(
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
    public static TraceEventResponse from(TraceEvent event) {
        return new TraceEventResponse(
                event.id(),
                event.sessionId(),
                event.eventType(),
                event.occurredAt(),
                event.screenName(),
                event.httpMethod(),
                event.path(),
                event.sourceSymbol(),
                event.traceId(),
                event.spanId(),
                event.metadata()
        );
    }
}
