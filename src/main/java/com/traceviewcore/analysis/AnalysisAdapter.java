package com.traceviewcore.analysis;

public interface AnalysisAdapter {

    String id();

    void apply(AnalysisContext context, MutableAnalysisGraph graph);
}
