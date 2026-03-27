package com.traceviewcore.analysis.service;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(50)
public class SpringInvocationAnalyzer implements CodeAnalyzer {

    private final SpringSourceParser parser;
    private final EvidenceFactory evidenceFactory;

    public SpringInvocationAnalyzer(SpringSourceParser parser, EvidenceFactory evidenceFactory) {
        this.parser = parser;
        this.evidenceFactory = evidenceFactory;
    }

    @Override
    public void analyze(AnalysisContext context, MutableAnalysisGraph graph) {
        Map<String, SpringSourceParser.ParsedClass> classes = new HashMap<>();
        Set<String> repositoryTypes = new HashSet<>();
        Set<String> externalTypes = new HashSet<>();

        for (SourceDocument sourceDocument : context.sourceDocuments()) {
            SpringSourceParser.ParsedClass parsedClass = parser.parse(sourceDocument);
            if (parsedClass.typeName() != null) {
                classes.put(parsedClass.typeName(), parsedClass);
                if (isRepositoryLike(parsedClass)) {
                    repositoryTypes.add(parsedClass.typeName());
                }
                if (isExternalLike(parsedClass)) {
                    externalTypes.add(parsedClass.typeName());
                }
            }
        }

        for (SpringSourceParser.ParsedClass parsedClass : classes.values()) {
            if (parsedClass.hasAnnotation("RestController") || parsedClass.hasAnnotation("Controller")) {
                mapControllerToService(parsedClass, graph);
            }
            if (parsedClass.hasAnnotation("Service") || parsedClass.hasAnnotation("Component")) {
                mapServiceDependencies(parsedClass, repositoryTypes, externalTypes, classes, graph);
            }
        }
    }

    private void mapControllerToService(SpringSourceParser.ParsedClass parsedClass, MutableAnalysisGraph graph) {
        Map<String, String> fieldTypeByName = fieldTypeByName(parsedClass);
        for (SpringSourceParser.ParsedMethod method : parsedClass.methods()) {
            for (Map.Entry<String, String> entry : fieldTypeByName.entrySet()) {
                Matcher matcher = Pattern.compile(entry.getKey() + "\\.(\\w+)\\(").matcher(method.body());
                while (matcher.find()) {
                    String serviceMethod = matcher.group(1);
                    String fromId = NodeIdFactory.entryPoint(parsedClass.typeName(), method.methodName());
                    String toId = NodeIdFactory.service(entry.getValue(), serviceMethod);
                    addRelation(graph, fromId, toId, RelationType.INVOKES, method, parsedClass, "SPRING_CONTROLLER_SERVICE_CALL");
                }
            }
        }
    }

    private void mapServiceDependencies(
            SpringSourceParser.ParsedClass parsedClass,
            Set<String> repositoryTypes,
            Set<String> externalTypes,
            Map<String, SpringSourceParser.ParsedClass> classes,
            MutableAnalysisGraph graph
    ) {
        Map<String, String> fieldTypeByName = fieldTypeByName(parsedClass);
        for (SpringSourceParser.ParsedMethod method : parsedClass.methods()) {
            String fromId = NodeIdFactory.service(parsedClass.typeName(), method.methodName());
            for (Map.Entry<String, String> entry : fieldTypeByName.entrySet()) {
                Matcher matcher = Pattern.compile(Pattern.quote(entry.getKey()) + "\\.(\\w+)\\(").matcher(method.body());
                Set<String> calledMethods = new HashSet<>();
                while (matcher.find()) {
                    calledMethods.add(matcher.group(1));
                }
                if (calledMethods.isEmpty()) {
                    continue;
                }

                if (isRepositoryType(entry.getValue(), repositoryTypes, classes)) {
                    ensureRepositoryNode(graph, parsedClass, method, entry.getValue());
                    addRelation(
                            graph,
                            fromId,
                            NodeIdFactory.repository(entry.getValue()),
                            RelationType.ACCESSES,
                            method,
                            parsedClass,
                            "SPRING_SERVICE_REPOSITORY_CALL"
                    );
                } else if (isExternalType(entry.getValue(), externalTypes, classes)) {
                    ensureExternalNode(graph, parsedClass, method, entry.getValue());
                    addRelation(
                            graph,
                            fromId,
                            NodeIdFactory.external(entry.getValue()),
                            RelationType.CALLS,
                            method,
                            parsedClass,
                            "SPRING_SERVICE_EXTERNAL_CALL"
                    );
                } else if (isServiceType(entry.getValue(), classes)) {
                    for (String serviceMethod : calledMethods) {
                        addRelation(
                                graph,
                                fromId,
                                NodeIdFactory.service(entry.getValue(), serviceMethod),
                                RelationType.INVOKES,
                                method,
                                parsedClass,
                                "SPRING_SERVICE_SERVICE_CALL"
                        );
                    }
                }
            }
        }
    }

    private Map<String, String> fieldTypeByName(SpringSourceParser.ParsedClass parsedClass) {
        Map<String, String> fieldTypes = new HashMap<>();
        parsedClass.fields().forEach(field -> fieldTypes.put(field.fieldName(), field.typeName().replaceAll("<.*>", "").trim()));
        return fieldTypes;
    }

    private boolean isRepositoryLike(SpringSourceParser.ParsedClass parsedClass) {
        return parsedClass.hasAnnotation("Repository")
                || parsedClass.content().contains("JpaRepository")
                || parsedClass.content().contains("CrudRepository");
    }

    private boolean isExternalLike(SpringSourceParser.ParsedClass parsedClass) {
        return parsedClass.content().contains("@FeignClient")
                || parsedClass.content().contains("WebClient")
                || parsedClass.content().contains("RestTemplate");
    }

    private boolean isRepositoryType(
            String typeName,
            Set<String> repositoryTypes,
            Map<String, SpringSourceParser.ParsedClass> classes
    ) {
        return repositoryTypes.contains(typeName)
                || typeName.endsWith("Repository")
                || classes.containsKey(typeName) && isRepositoryLike(classes.get(typeName));
    }

    private boolean isExternalType(
            String typeName,
            Set<String> externalTypes,
            Map<String, SpringSourceParser.ParsedClass> classes
    ) {
        return externalTypes.contains(typeName)
                || typeName.contains("WebClient")
                || typeName.contains("RestTemplate")
                || typeName.contains("Feign")
                || classes.containsKey(typeName) && isExternalLike(classes.get(typeName));
    }

    private boolean isServiceType(String typeName, Map<String, SpringSourceParser.ParsedClass> classes) {
        if (typeName.endsWith("Service")) {
            return true;
        }
        SpringSourceParser.ParsedClass parsedClass = classes.get(typeName);
        return parsedClass != null && (parsedClass.hasAnnotation("Service") || parsedClass.hasAnnotation("Component"));
    }

    private void ensureRepositoryNode(
            MutableAnalysisGraph graph,
            SpringSourceParser.ParsedClass parsedClass,
            SpringSourceParser.ParsedMethod method,
            String typeName
    ) {
        if (graph.findNode(NodeIdFactory.repository(typeName)).isPresent()) {
            return;
        }
        var evidence = evidenceFactory.create(
                "spring_repository_reference",
                parsedClass.path(),
                method.sourceLine(),
                parsedClass.typeName() + "#" + method.methodName(),
                "SPRING_REPOSITORY_REFERENCE",
                getClass().getSimpleName()
        );
        graph.addEvidence(evidence);
        graph.addNode(new GraphNode(
                NodeIdFactory.repository(typeName),
                NodeType.REPOSITORY_CALL,
                typeName,
                parsedClass.path(),
                typeName,
                0.75d,
                com.traceviewcore.domain.ReviewStatus.DRAFT,
                Set.of("spring", "repository", "inferred"),
                Map.of("repositoryType", typeName)
        ));
    }

    private void ensureExternalNode(
            MutableAnalysisGraph graph,
            SpringSourceParser.ParsedClass parsedClass,
            SpringSourceParser.ParsedMethod method,
            String typeName
    ) {
        if (graph.findNode(NodeIdFactory.external(typeName)).isPresent()) {
            return;
        }
        var evidence = evidenceFactory.create(
                "spring_external_reference",
                parsedClass.path(),
                method.sourceLine(),
                parsedClass.typeName() + "#" + method.methodName(),
                "SPRING_EXTERNAL_REFERENCE",
                getClass().getSimpleName()
        );
        graph.addEvidence(evidence);
        graph.addNode(new GraphNode(
                NodeIdFactory.external(typeName),
                NodeType.EXTERNAL_CALL,
                typeName,
                parsedClass.path(),
                typeName,
                0.75d,
                com.traceviewcore.domain.ReviewStatus.DRAFT,
                Set.of("spring", "external", "inferred"),
                Map.of("clientType", typeName)
        ));
    }

    private void addRelation(
            MutableAnalysisGraph graph,
            String fromId,
            String toId,
            RelationType relationType,
            SpringSourceParser.ParsedMethod method,
            SpringSourceParser.ParsedClass parsedClass,
            String ruleId
    ) {
        var evidence = evidenceFactory.create(
                "spring_invocation",
                parsedClass.path(),
                method.sourceLine(),
                parsedClass.typeName() + "#" + method.methodName(),
                ruleId,
                getClass().getSimpleName()
        );
        graph.addEvidence(evidence);
        graph.addRelation(new GraphRelation(
                IdGenerator.newId("relation"),
                fromId,
                toId,
                relationType,
                CertaintyType.INFERRED,
                0.8d,
                java.util.List.of(evidence.id())
        ));
    }
}
