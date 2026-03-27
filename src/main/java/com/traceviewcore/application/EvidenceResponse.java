package com.traceviewcore.application;

import com.traceviewcore.domain.Evidence;
import java.time.Instant;

public record EvidenceResponse(
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

    public static EvidenceResponse from(Evidence evidence) {
        return new EvidenceResponse(
                evidence.id(),
                evidence.evidenceType(),
                evidence.sourceFile(),
                evidence.sourceLine(),
                evidence.sourceSymbol(),
                evidence.ruleId(),
                evidence.analyzerName(),
                evidence.analyzerVersion(),
                evidence.capturedAt()
        );
    }
}
