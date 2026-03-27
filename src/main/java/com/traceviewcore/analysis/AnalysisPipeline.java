package com.traceviewcore.analysis;

import com.traceviewcore.domain.AnalysisGraph;

public interface AnalysisPipeline {

    default AnalysisGraph analyze(AnalysisContext context) {
        return analyze(context, "spring-standard");
    }

    AnalysisGraph analyze(AnalysisContext context, String adapterId);
}
