package com.traceviewcore.analysis;

import com.traceviewcore.domain.AnalysisGraph;

public interface AnalysisPipeline {

    AnalysisGraph analyze(AnalysisContext context);
}
