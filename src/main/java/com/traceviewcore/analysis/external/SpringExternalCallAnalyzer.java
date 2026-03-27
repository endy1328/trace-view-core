package com.traceviewcore.analysis.external;

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
@Order(40)
public class SpringExternalCallAnalyzer implements CodeAnalyzer {

    private final SpringSourceParser parser;
    private final EvidenceFactory evidenceFactory;

    public SpringExternalCallAnalyzer(SpringSourceParser parser, EvidenceFactory evidenceFactory) {
        this.parser = parser;
        this.evidenceFactory = evidenceFactory;
    }

    @Override
    public void analyze(AnalysisContext context, MutableAnalysisGraph graph) {
        for (SourceDocument sourceDocument : context.sourceDocuments()) {
            SpringSourceParser.ParsedClass parsedClass = parser.parse(sourceDocument);
            boolean externalClient = isExternalClient(parsedClass);
            if (!externalClient || parsedClass.typeName() == null) {
                continue;
            }
            var evidence = evidenceFactory.create(
                    "spring_external_call",
                    parsedClass.path(),
                    1,
                    parsedClass.typeName(),
                    "SPRING_EXTERNAL_CLIENT",
                    getClass().getSimpleName()
            );
            graph.addEvidence(evidence);
            graph.addNode(new GraphNode(
                    NodeIdFactory.external(parsedClass.typeName()),
                    NodeType.EXTERNAL_CALL,
                    parsedClass.typeName(),
                    parsedClass.path(),
                    parsedClass.typeName(),
                    0.85d,
                    ReviewStatus.DRAFT,
                    Set.of("spring", "external"),
                    Map.of("clientType", detectClientType(parsedClass.content()))
                ));
        }
    }

    private boolean isExternalClient(SpringSourceParser.ParsedClass parsedClass) {
        return parsedClass.content().contains("RestTemplate")
                || parsedClass.content().contains("WebClient")
                || parsedClass.content().contains("@FeignClient")
                || parsedClass.supertypes().stream().anyMatch(type -> type.contains("Feign"));
    }

    private String detectClientType(String content) {
        if (content.contains("@FeignClient")) {
            return "FeignClient";
        }
        if (content.contains("WebClient")) {
            return "WebClient";
        }
        return "RestTemplate";
    }
}
