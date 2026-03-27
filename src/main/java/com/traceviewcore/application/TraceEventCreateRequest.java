package com.traceviewcore.application;

import com.traceviewcore.domain.TraceEventType;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

public record TraceEventCreateRequest(
        @NotNull TraceEventType eventType,
        @NotNull Instant occurredAt,
        String screenName,
        String httpMethod,
        String path,
        String sourceSymbol,
        String traceId,
        String spanId,
        Map<String, String> metadata
) {
}
