package com.traceviewcore.analysis.repository;

import com.traceviewcore.analysis.AnalysisContext;
import com.traceviewcore.analysis.CodeAnalyzer;
import com.traceviewcore.analysis.MutableAnalysisGraph;
import com.traceviewcore.analysis.SourceDocument;
import com.traceviewcore.analysis.SpringSourceParser;
import com.traceviewcore.analysis.evidence.EvidenceFactory;
import com.traceviewcore.common.NodeIdFactory;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.NodeType;
import com.traceviewcore.domain.ReviewStatus;
import java.util.Map;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
public class SpringRepositoryAnalyzer implements CodeAnalyzer {

    private final SpringSourceParser parser;
    private final EvidenceFactory evidenceFactory;

    public SpringRepositoryAnalyzer(SpringSourceParser parser, EvidenceFactory evidenceFactory) {
        this.parser = parser;
        this.evidenceFactory = evidenceFactory;
    }

    @Override
    public void analyze(AnalysisContext context, MutableAnalysisGraph graph) {
        for (SourceDocument sourceDocument : context.sourceDocuments()) {
            SpringSourceParser.ParsedClass parsedClass = parser.parse(sourceDocument);
            boolean repositoryLike = isRepositoryLike(parsedClass);
            if (!repositoryLike || parsedClass.typeName() == null) {
                continue;
            }
            var evidence = evidenceFactory.create(
                    "spring_repository_type",
                    parsedClass.path(),
                    1,
                    parsedClass.typeName(),
                    "SPRING_REPOSITORY_TYPE",
                    getClass().getSimpleName()
            );
            graph.addEvidence(evidence);
            graph.addNode(new GraphNode(
                    NodeIdFactory.repository(parsedClass.typeName()),
                    NodeType.REPOSITORY_CALL,
                    parsedClass.typeName(),
                    parsedClass.path(),
                    parsedClass.typeName(),
                    0.9d,
                    ReviewStatus.DRAFT,
                    Set.of("spring", "repository"),
                    Map.of("repositoryType", parsedClass.typeName())
                ));
        }
    }

    private boolean isRepositoryLike(SpringSourceParser.ParsedClass parsedClass) {
        return parsedClass.hasAnnotation("Repository")
                || parsedClass.content().contains("JpaRepository")
                || parsedClass.content().contains("CrudRepository")
                || parsedClass.supertypes().stream().anyMatch(type -> type.endsWith("Repository"));
    }
}
