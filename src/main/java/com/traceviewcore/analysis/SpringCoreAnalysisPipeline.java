package com.traceviewcore.analysis;

import com.traceviewcore.domain.AnalysisGraph;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpringCoreAnalysisPipeline implements AnalysisPipeline {

    private final List<CodeAnalyzer> analyzers;
    private final Map<String, AnalysisAdapter> adaptersById;

    public SpringCoreAnalysisPipeline(List<CodeAnalyzer> analyzers) {
        this(analyzers, List.of());
    }

    @Autowired
    public SpringCoreAnalysisPipeline(List<CodeAnalyzer> analyzers, List<AnalysisAdapter> adapters) {
        this.analyzers = analyzers;
        this.adaptersById = new LinkedHashMap<>();
        adapters.forEach(adapter -> this.adaptersById.put(adapter.id(), adapter));
    }

    @Override
    public AnalysisGraph analyze(AnalysisContext context, String adapterId) {
        MutableAnalysisGraph graph = new MutableAnalysisGraph();
        analyzers.forEach(analyzer -> analyzer.analyze(context, graph));
        if (adapterId != null && !adapterId.isBlank() && !"spring-standard".equals(adapterId)) {
            AnalysisAdapter adapter = adaptersById.get(adapterId);
            if (adapter == null) {
                throw new IllegalArgumentException("Analysis adapter not found: " + adapterId);
            }
            adapter.apply(context, graph);
        }
        return graph.toImmutableGraph();
    }
}
