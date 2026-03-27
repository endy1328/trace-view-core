package com.traceviewcore.analysis.adapter;

import com.traceviewcore.analysis.AnalysisAdapter;
import com.traceviewcore.analysis.AnalysisContext;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class AStoreLegacyAnalysisAdapter implements AnalysisAdapter {

    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\bimplements\\s+([^\\{]+)");

    private final SpringSourceParser parser;
    private final EvidenceFactory evidenceFactory;

    public AStoreLegacyAnalysisAdapter(SpringSourceParser parser, EvidenceFactory evidenceFactory) {
        this.parser = parser;
        this.evidenceFactory = evidenceFactory;
    }

    @Override
    public String id() {
        return "astore-legacy";
    }

    @Override
    public void apply(AnalysisContext context, MutableAnalysisGraph graph) {
        Map<String, SpringSourceParser.ParsedClass> classes = new HashMap<>();
        for (SourceDocument sourceDocument : context.sourceDocuments()) {
            SpringSourceParser.ParsedClass parsedClass = parser.parse(sourceDocument);
            if (parsedClass.typeName() != null) {
                classes.put(parsedClass.typeName(), parsedClass);
            }
        }

        classes.values().forEach(parsedClass -> ensureServiceNodes(parsedClass, graph));
        classes.values().forEach(parsedClass -> ensureRepositoryNodes(parsedClass, graph));
        classes.values().forEach(parsedClass -> mapControllerToServiceLike(parsedClass, graph));
        classes.values().forEach(parsedClass -> mapServiceLikeDependencies(parsedClass, graph));
    }

    private void ensureServiceNodes(SpringSourceParser.ParsedClass parsedClass, MutableAnalysisGraph graph) {
        String serviceAlias = canonicalServiceAlias(parsedClass);
        if (serviceAlias == null) {
            return;
        }
        for (SpringSourceParser.ParsedMethod method : parsedClass.methods()) {
            if (!method.body().contains("{")) {
                continue;
            }
            var evidence = evidenceFactory.create(
                    "astore_service_method",
                    parsedClass.path(),
                    method.sourceLine(),
                    parsedClass.typeName() + "#" + method.methodName(),
                    "ASTORE_SERVICE_METHOD",
                    getClass().getSimpleName()
            );
            graph.addEvidence(evidence);
            graph.addNode(new GraphNode(
                    NodeIdFactory.service(serviceAlias, method.methodName()),
                    NodeType.SERVICE_METHOD,
                    serviceAlias + "#" + method.methodName(),
                    parsedClass.path(),
                    parsedClass.typeName() + "#" + method.methodName(),
                    confidenceFor(serviceAlias, parsedClass.typeName(), 0.9d, 0.82d),
                    ReviewStatus.DRAFT,
                    Set.of("astore", "service", "adapter"),
                    Map.of(
                            "className", parsedClass.typeName(),
                            "serviceAlias", serviceAlias,
                            "methodName", method.methodName()
                    )
            ));
        }
    }

    private void ensureRepositoryNodes(SpringSourceParser.ParsedClass parsedClass, MutableAnalysisGraph graph) {
        String repositoryAlias = canonicalRepositoryAlias(parsedClass);
        if (repositoryAlias == null) {
            return;
        }
        var evidence = evidenceFactory.create(
                "astore_repository_type",
                parsedClass.path(),
                1,
                parsedClass.typeName(),
                "ASTORE_REPOSITORY_TYPE",
                getClass().getSimpleName()
        );
        graph.addEvidence(evidence);
        graph.addNode(new GraphNode(
                NodeIdFactory.repository(repositoryAlias),
                NodeType.REPOSITORY_CALL,
                repositoryAlias,
                parsedClass.path(),
                parsedClass.typeName(),
                confidenceFor(repositoryAlias, parsedClass.typeName(), 0.88d, 0.8d),
                ReviewStatus.DRAFT,
                Set.of("astore", "repository", "adapter"),
                Map.of(
                        "className", parsedClass.typeName(),
                        "repositoryAlias", repositoryAlias
                )
        ));
    }

    private void mapControllerToServiceLike(SpringSourceParser.ParsedClass parsedClass, MutableAnalysisGraph graph) {
        if (!(parsedClass.hasAnnotation("Controller") || parsedClass.hasAnnotation("RestController"))) {
            return;
        }
        Map<String, String> fieldTypeByName = fieldTypeByName(parsedClass);
        for (SpringSourceParser.ParsedMethod method : parsedClass.methods()) {
            for (Map.Entry<String, String> entry : fieldTypeByName.entrySet()) {
                if (!isServiceLikeTypeName(entry.getValue())) {
                    continue;
                }
                Matcher matcher = Pattern.compile(Pattern.quote(entry.getKey()) + "\\.(\\w+)\\(").matcher(method.body());
                while (matcher.find()) {
                    addRelation(
                            graph,
                            NodeIdFactory.entryPoint(parsedClass.typeName(), method.methodName()),
                            NodeIdFactory.service(entry.getValue(), matcher.group(1)),
                            RelationType.INVOKES,
                            parsedClass,
                            method,
                            "ASTORE_CONTROLLER_SERVICE_CALL"
                    );
                }
            }
        }
    }

    private void mapServiceLikeDependencies(SpringSourceParser.ParsedClass parsedClass, MutableAnalysisGraph graph) {
        String serviceAlias = canonicalServiceAlias(parsedClass);
        if (serviceAlias == null) {
            return;
        }
        Map<String, String> fieldTypeByName = fieldTypeByName(parsedClass);
        for (SpringSourceParser.ParsedMethod method : parsedClass.methods()) {
            for (Map.Entry<String, String> entry : fieldTypeByName.entrySet()) {
                Matcher matcher = Pattern.compile(Pattern.quote(entry.getKey()) + "\\.(\\w+)\\(").matcher(method.body());
                Set<String> calledMethods = new LinkedHashSet<>();
                while (matcher.find()) {
                    calledMethods.add(matcher.group(1));
                }
                if (calledMethods.isEmpty()) {
                    continue;
                }
                String fromId = NodeIdFactory.service(serviceAlias, method.methodName());
                if (isRepositoryLikeTypeName(entry.getValue())) {
                    addRelation(graph, fromId, NodeIdFactory.repository(entry.getValue()), RelationType.ACCESSES, parsedClass, method, "ASTORE_SERVICE_DAO_CALL");
                } else if (isServiceLikeTypeName(entry.getValue())) {
                    for (String calledMethod : calledMethods) {
                        addRelation(graph, fromId, NodeIdFactory.service(entry.getValue(), calledMethod), RelationType.INVOKES, parsedClass, method, "ASTORE_SERVICE_SERVICE_CALL");
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

    private String canonicalServiceAlias(SpringSourceParser.ParsedClass parsedClass) {
        List<String> preferredAliases = new ArrayList<>();
        declaredInterfaces(parsedClass).stream()
                .filter(this::isServiceLikeTypeName)
                .forEach(preferredAliases::add);
        parsedClass.supertypes().stream()
                .filter(this::isServiceLikeTypeName)
                .filter(typeName -> !isConcreteLegacyService(typeName))
                .forEach(preferredAliases::add);
        if (isServiceLikeClass(parsedClass)) {
            preferredAliases.add(parsedClass.typeName());
        }
        return preferredAliases.stream()
                .filter(alias -> alias != null && !alias.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String canonicalRepositoryAlias(SpringSourceParser.ParsedClass parsedClass) {
        List<String> preferredAliases = new ArrayList<>();
        declaredInterfaces(parsedClass).stream()
                .filter(this::isRepositoryLikeTypeName)
                .forEach(preferredAliases::add);
        parsedClass.supertypes().stream()
                .filter(this::isRepositoryLikeTypeName)
                .filter(typeName -> !isConcreteLegacyRepository(typeName))
                .forEach(preferredAliases::add);
        if (isRepositoryLikeClass(parsedClass)) {
            preferredAliases.add(parsedClass.typeName());
        }
        return preferredAliases.stream()
                .filter(alias -> alias != null && !alias.isBlank())
                .findFirst()
                .orElse(null);
    }

    private List<String> declaredInterfaces(SpringSourceParser.ParsedClass parsedClass) {
        Matcher matcher = IMPLEMENTS_PATTERN.matcher(parsedClass.content());
        if (!matcher.find()) {
            return List.of();
        }
        List<String> interfaces = new ArrayList<>();
        for (String rawType : matcher.group(1).split(",")) {
            String normalized = rawType.replaceAll("<.*?>", "").trim();
            if (!normalized.isBlank()) {
                interfaces.add(normalized);
            }
        }
        return interfaces;
    }

    private boolean isServiceLikeClass(SpringSourceParser.ParsedClass parsedClass) {
        return parsedClass.hasAnnotation("Service")
                || parsedClass.hasAnnotation("Component")
                || isServiceLikeTypeName(parsedClass.typeName())
                || parsedClass.supertypes().stream().anyMatch(this::isServiceLikeTypeName);
    }

    private boolean isRepositoryLikeClass(SpringSourceParser.ParsedClass parsedClass) {
        return isRepositoryLikeTypeName(parsedClass.typeName())
                || parsedClass.supertypes().stream().anyMatch(this::isRepositoryLikeTypeName)
                || parsedClass.content().contains("SqlMapClientDaoSupport")
                || parsedClass.content().contains("BaseSqlMapDaoSupport");
    }

    private boolean isServiceLikeTypeName(String typeName) {
        return typeName != null && (
                typeName.endsWith("Service")
                        || typeName.endsWith("ServiceImpl")
                        || typeName.endsWith("Biz")
                        || typeName.endsWith("BizImpl")
        );
    }

    private boolean isRepositoryLikeTypeName(String typeName) {
        return typeName != null && (
                typeName.endsWith("DAO")
                        || typeName.endsWith("DAOImpl")
                        || typeName.endsWith("Dao")
                        || typeName.endsWith("DaoImpl")
                        || typeName.endsWith("Repository")
        );
    }

    private boolean isConcreteLegacyService(String typeName) {
        return typeName != null && (typeName.endsWith("ServiceImpl") || typeName.endsWith("BizImpl"));
    }

    private boolean isConcreteLegacyRepository(String typeName) {
        return typeName != null && (typeName.endsWith("DAOImpl") || typeName.endsWith("DaoImpl"));
    }

    private double confidenceFor(String alias, String concreteType, double exactConfidence, double adaptedConfidence) {
        return alias.equals(concreteType) ? exactConfidence : adaptedConfidence;
    }

    private void addRelation(
            MutableAnalysisGraph graph,
            String fromId,
            String toId,
            RelationType relationType,
            SpringSourceParser.ParsedClass parsedClass,
            SpringSourceParser.ParsedMethod method,
            String ruleId
    ) {
        var evidence = evidenceFactory.create(
                "astore_invocation",
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
                0.78d,
                List.of(evidence.id())
        ));
    }
}
