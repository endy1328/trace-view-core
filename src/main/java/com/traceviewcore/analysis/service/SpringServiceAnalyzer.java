package com.traceviewcore.analysis.service;

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
@Order(20)
public class SpringServiceAnalyzer implements CodeAnalyzer {

    private final SpringSourceParser parser;
    private final EvidenceFactory evidenceFactory;

    public SpringServiceAnalyzer(SpringSourceParser parser, EvidenceFactory evidenceFactory) {
        this.parser = parser;
        this.evidenceFactory = evidenceFactory;
    }

    @Override
    public void analyze(AnalysisContext context, MutableAnalysisGraph graph) {
        for (SourceDocument sourceDocument : context.sourceDocuments()) {
            SpringSourceParser.ParsedClass parsedClass = parser.parse(sourceDocument);
            if (!parsedClass.hasAnnotation("Service") && !parsedClass.hasAnnotation("Component")) {
                continue;
            }
            for (SpringSourceParser.ParsedMethod method : parsedClass.methods()) {
                if (!method.body().contains("{")) {
                    continue;
                }
                var evidence = evidenceFactory.create(
                        "spring_service_method",
                        parsedClass.path(),
                        method.sourceLine(),
                        parsedClass.typeName() + "#" + method.methodName(),
                        "SPRING_SERVICE_METHOD",
                        getClass().getSimpleName()
                );
                graph.addEvidence(evidence);
                graph.addNode(new GraphNode(
                        NodeIdFactory.service(parsedClass.typeName(), method.methodName()),
                        NodeType.SERVICE_METHOD,
                        parsedClass.typeName() + "#" + method.methodName(),
                        parsedClass.path(),
                        parsedClass.typeName() + "#" + method.methodName(),
                        0.95d,
                        ReviewStatus.DRAFT,
                        Set.of("spring", "service"),
                        Map.of("className", parsedClass.typeName(), "methodName", method.methodName())
                ));
            }
        }
    }
}
