package com.traceviewcore.application;

import com.traceviewcore.analysis.AnalysisContext;
import com.traceviewcore.analysis.AnalysisPipeline;
import com.traceviewcore.analysis.SourceScanner;
import com.traceviewcore.common.IdGenerator;
import com.traceviewcore.domain.AnalysisSnapshot;
import com.traceviewcore.persistence.GraphStore;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AnalysisOrchestrator {

    private final SourceScanner sourceScanner;
    private final AnalysisPipeline analysisPipeline;
    private final GraphStore graphStore;

    public AnalysisOrchestrator(SourceScanner sourceScanner, AnalysisPipeline analysisPipeline, GraphStore graphStore) {
        this.sourceScanner = sourceScanner;
        this.analysisPipeline = analysisPipeline;
        this.graphStore = graphStore;
    }

    public AnalysisRunResponse run(String rootPath) throws IOException {
        return run(rootPath, null);
    }

    public AnalysisRunResponse run(String rootPath, String adapterId) throws IOException {
        AnalysisContext context = sourceScanner.scan(Path.of(rootPath));
        String resolvedAdapterId = adapterId == null || adapterId.isBlank() ? "spring-standard" : adapterId;
        var graph = analysisPipeline.analyze(context, resolvedAdapterId);
        AnalysisSnapshot snapshot = new AnalysisSnapshot(
                IdGenerator.newId("snapshot"),
                rootPath,
                Instant.now(),
                graph
        );
        graphStore.save(snapshot);
        return new AnalysisRunResponse(
                snapshot.id(),
                snapshot.rootPath(),
                resolvedAdapterId,
                snapshot.createdAt(),
                graph.nodes().size(),
                graph.relations().size(),
                graph.evidences().size()
        );
    }
}
