package com.traceviewcore.domain;

import java.time.Instant;

public record Evidence(
        String id,
        String evidenceType,
        String sourceFile,
        Integer sourceLine,
        String sourceSymbol,
        String ruleId,
        String analyzerName,
        String analyzerVersion,
        Instant capturedAt
) {
}
