package com.traceviewcore.analysis.springmvc;

import com.traceviewcore.analysis.AnalysisContext;
import com.traceviewcore.analysis.CodeAnalyzer;
import com.traceviewcore.analysis.MutableAnalysisGraph;
import com.traceviewcore.analysis.SourceDocument;
import com.traceviewcore.analysis.SpringSourceParser;
import com.traceviewcore.analysis.evidence.EvidenceFactory;
import com.traceviewcore.common.IdGenerator;
import com.traceviewcore.common.NodeIdFactory;
import com.traceviewcore.domain.CertaintyType;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.GraphRelation;
import com.traceviewcore.domain.NodeType;
import com.traceviewcore.domain.RelationType;
import com.traceviewcore.domain.ReviewStatus;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class SpringEndpointAnalyzer implements CodeAnalyzer {

    private static final Pattern CLASS_REQUEST_MAPPING = Pattern.compile("@RequestMapping\\(([^)]*)\\)");
    private static final Pattern METHOD_MAPPING = Pattern.compile("@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\\(([^)]*)\\)");

    private final SpringSourceParser parser;
    private final EvidenceFactory evidenceFactory;

    public SpringEndpointAnalyzer(SpringSourceParser parser, EvidenceFactory evidenceFactory) {
        this.parser = parser;
        this.evidenceFactory = evidenceFactory;
    }

    @Override
    public void analyze(AnalysisContext context, MutableAnalysisGraph graph) {
        for (SourceDocument sourceDocument : context.sourceDocuments()) {
            SpringSourceParser.ParsedClass parsedClass = parser.parse(sourceDocument);
            if (!parsedClass.hasAnnotation("RestController") && !parsedClass.hasAnnotation("Controller")) {
                continue;
            }

            String classPath = extractPath(CLASS_REQUEST_MAPPING, parsedClass.content());
            for (SpringSourceParser.ParsedMethod method : parsedClass.methods()) {
                if (method.annotationLine() == null) {
                    continue;
                }
                Matcher matcher = METHOD_MAPPING.matcher(method.annotationLine());
                if (!matcher.find()) {
                    continue;
                }
                String httpMethod = normalizeHttpMethod(matcher.group(1), matcher.group(2));
                String methodPath = extractPath(matcher.group(2));
                String fullPath = normalizePath(classPath, methodPath);

                String endpointId = NodeIdFactory.endpoint(httpMethod, fullPath);
                String entryPointId = NodeIdFactory.entryPoint(parsedClass.typeName(), method.methodName());
                var evidence = evidenceFactory.create(
                        "spring_endpoint_mapping",
                        parsedClass.path(),
                        method.sourceLine(),
                        parsedClass.typeName() + "#" + method.methodName(),
                        "SPRING_MVC_ENDPOINT",
                        getClass().getSimpleName()
                );
                graph.addEvidence(evidence);
                graph.addNode(new GraphNode(
                        endpointId,
                        NodeType.API_ENDPOINT,
                        httpMethod + " " + fullPath,
                        parsedClass.path(),
                        fullPath,
                        1.0d,
                        ReviewStatus.DRAFT,
                        Set.of("spring", "endpoint"),
                        Map.of("httpMethod", httpMethod, "path", fullPath)
                ));
                graph.addNode(new GraphNode(
                        entryPointId,
                        NodeType.BACKEND_ENTRY_POINT,
                        parsedClass.typeName() + "#" + method.methodName(),
                        parsedClass.path(),
                        parsedClass.typeName() + "#" + method.methodName(),
                        1.0d,
                        ReviewStatus.DRAFT,
                        Set.of("spring", "controller"),
                        Map.of("className", parsedClass.typeName(), "methodName", method.methodName())
                ));
                graph.addRelation(new GraphRelation(
                        IdGenerator.newId("relation"),
                        endpointId,
                        entryPointId,
                        RelationType.HANDLED_BY,
                        CertaintyType.CONFIRMED,
                        1.0d,
                        java.util.List.of(evidence.id())
                ));
            }
        }
    }

    private String normalizeHttpMethod(String annotationName, String rawArguments) {
        return switch (annotationName) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "PutMapping" -> "PUT";
            case "DeleteMapping" -> "DELETE";
            case "PatchMapping" -> "PATCH";
            default -> extractRequestMethod(rawArguments);
        };
    }

    private String extractRequestMethod(String rawArguments) {
        Matcher matcher = Pattern.compile("RequestMethod\\.([A-Z]+)").matcher(rawArguments);
        return matcher.find() ? matcher.group(1) : "GET";
    }

    private String extractPath(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? extractPath(matcher.group(1)) : "";
    }

    private String extractPath(String rawArguments) {
        Matcher valueMatcher = Pattern.compile("\"([^\"]*)\"").matcher(rawArguments);
        return valueMatcher.find() ? valueMatcher.group(1) : "";
    }

    private String normalizePath(String classPath, String methodPath) {
        String combined = (classPath + "/" + methodPath).replaceAll("//+", "/");
        return combined.startsWith("/") ? combined : "/" + combined;
    }
}
