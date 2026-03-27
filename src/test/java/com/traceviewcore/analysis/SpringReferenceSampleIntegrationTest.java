package com.traceviewcore.analysis;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.traceviewcore.analysis.evidence.EvidenceFactory;
import com.traceviewcore.analysis.external.SpringExternalCallAnalyzer;
import com.traceviewcore.analysis.repository.SpringRepositoryAnalyzer;
import com.traceviewcore.analysis.service.SpringInvocationAnalyzer;
import com.traceviewcore.analysis.service.SpringServiceAnalyzer;
import com.traceviewcore.analysis.springmvc.SpringEndpointAnalyzer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpringReferenceSampleIntegrationTest {

    private final SourceScanner sourceScanner = new SourceScanner();
    private final SpringSourceParser parser = new SpringSourceParser();
    private final EvidenceFactory evidenceFactory = new EvidenceFactory();

    @Test
    void analyzesEmbeddedSpringReferenceSample() throws IOException {
        Path sampleRoot = Path.of("samples/spring-reference-app");
        AnalysisContext context = sourceScanner.scan(sampleRoot);
        SpringCoreAnalysisPipeline pipeline = new SpringCoreAnalysisPipeline(List.of(
                new SpringEndpointAnalyzer(parser, evidenceFactory),
                new SpringServiceAnalyzer(parser, evidenceFactory),
                new SpringRepositoryAnalyzer(parser, evidenceFactory),
                new SpringExternalCallAnalyzer(parser, evidenceFactory),
                new SpringInvocationAnalyzer(parser, evidenceFactory)
        ));

        var graph = pipeline.analyze(context);

        assertTrue(graph.nodes().stream().anyMatch(node -> node.name().equals("GET /orders/{id}")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.name().equals("OrderController#findOrder")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.name().equals("OrderService#findOrder")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.name().equals("OrderRepository")));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.name().equals("BillingClient")));
        assertTrue(graph.relations().stream().anyMatch(relation -> relation.evidenceIds() != null && !relation.evidenceIds().isEmpty()));
    }
}
