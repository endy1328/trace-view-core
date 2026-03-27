package com.traceviewcore.analysis.evidence;

import com.traceviewcore.common.IdGenerator;
import com.traceviewcore.domain.Evidence;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class EvidenceFactory {

    public Evidence create(String evidenceType, String sourceFile, Integer sourceLine, String sourceSymbol, String ruleId, String analyzerName) {
        return new Evidence(
                IdGenerator.newId("evidence"),
                evidenceType,
                sourceFile,
                sourceLine,
                sourceSymbol,
                ruleId,
                analyzerName,
                "0.1.0",
                Instant.now()
        );
    }
}
