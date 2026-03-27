package com.traceviewcore.analysis;

import com.traceviewcore.domain.AnalysisGraph;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SpringCoreAnalysisPipeline implements AnalysisPipeline {

    private final List<CodeAnalyzer> analyzers;

    public SpringCoreAnalysisPipeline(List<CodeAnalyzer> analyzers) {
        this.analyzers = analyzers;
    }

    @Override
    public AnalysisGraph analyze(AnalysisContext context) {
        MutableAnalysisGraph graph = new MutableAnalysisGraph();
        analyzers.forEach(analyzer -> analyzer.analyze(context, graph));
        return graph.toImmutableGraph();
    }
}
