package com.traceviewcore.domain;

import java.time.Instant;

public record TraceSession(
        String id,
        String traceId,
        String environment,
        Instant collectedAt
) {
}
