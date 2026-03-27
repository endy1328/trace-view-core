package com.traceviewcore.application;

import com.traceviewcore.domain.TraceSession;
import java.time.Instant;

public record TraceSessionResponse(
        String id,
        String traceId,
        String environment,
        Instant collectedAt,
        int eventCount
) {
    public static TraceSessionResponse from(TraceSession session, int eventCount) {
        return new TraceSessionResponse(
                session.id(),
                session.traceId(),
                session.environment(),
                session.collectedAt(),
                eventCount
        );
    }
}
