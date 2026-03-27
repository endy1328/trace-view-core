package com.traceviewcore.application;

import com.traceviewcore.domain.AnalysisSnapshot;
import com.traceviewcore.domain.Evidence;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.GraphRelation;
import com.traceviewcore.domain.NodeType;
import com.traceviewcore.persistence.AnnotationStore;
import com.traceviewcore.persistence.GraphStore;
import java.util.Comparator;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class QueryApplicationService {

    private final GraphStore graphStore;
    private final AnnotationStore annotationStore;

    public QueryApplicationService(GraphStore graphStore, AnnotationStore annotationStore) {
        this.graphStore = graphStore;
        this.annotationStore = annotationStore;
    }

    public List<GraphNode> search(String query) {
        return graphStore.search(query);
    }

    public List<GraphNode> endpoints() {
        return graphStore.findByType("API_ENDPOINT");
    }

    public NodeDetailResponse node(String id) {
        AnalysisSnapshot snapshot = latest();
        GraphNode node = snapshot.graph().nodes().stream()
                .filter(candidate -> candidate.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Node not found: " + id));

        Map<String, GraphNode> nodesById = indexNodes(snapshot.graph().nodes());
        List<GraphRelation> relations = snapshot.graph().relations().stream()
                .filter(relation -> relation.fromId().equals(id) || relation.toId().equals(id))
                .toList();

        List<ConnectedNodeRelationResponse> incomingRelations = relations.stream()
                .filter(relation -> relation.toId().equals(id))
                .map(relation -> new ConnectedNodeRelationResponse(
                        toRelationResponse(relation, nodesById),
                        GraphNodeResponse.from(nodesById.get(relation.fromId()))))
                .toList();

        List<ConnectedNodeRelationResponse> outgoingRelations = relations.stream()
                .filter(relation -> relation.fromId().equals(id))
                .map(relation -> new ConnectedNodeRelationResponse(
                        toRelationResponse(relation, nodesById),
                        GraphNodeResponse.from(nodesById.get(relation.toId()))))
                .toList();

        Set<String> evidenceIds = relations.stream()
                .flatMap(relation -> relation.evidenceIds().stream())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<EvidenceResponse> evidences = snapshot.graph().evidences().stream()
                .filter(evidence -> evidenceIds.contains(evidence.id()))
                .map(EvidenceResponse::from)
                .toList();

        List<AnnotationResponse> annotations = annotationStore.findByTargetId(id).stream()
                .map(AnnotationResponse::from)
                .toList();

        return new NodeDetailResponse(
                GraphNodeResponse.from(node),
                incomingRelations,
                outgoingRelations,
                evidences,
                annotations
        );
    }

    public AnalysisSnapshot latest() {
        return graphStore.latest()
                .orElseThrow(() -> new IllegalStateException("No analysis snapshot available"));
    }

    public Optional<AnalysisSnapshot> latestOptional() {
        return graphStore.latest();
    }

    public GraphResponse graph(String nodeId, String type) {
        AnalysisSnapshot snapshot = latest();
        Map<String, GraphNode> nodesById = indexNodes(snapshot.graph().nodes());

        Set<String> selectedNodeIds = snapshot.graph().nodes().stream()
                .filter(node -> matchesType(node, type))
                .map(GraphNode::id)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        if (nodeId != null && !nodeId.isBlank()) {
            GraphNode centerNode = nodesById.get(nodeId);
            if (centerNode == null) {
                throw new IllegalArgumentException("Node not found: " + nodeId);
            }
            Set<String> centeredNodeIds = snapshot.graph().relations().stream()
                    .filter(relation -> relation.fromId().equals(nodeId) || relation.toId().equals(nodeId))
                    .collect(LinkedHashSet::new, (ids, relation) -> {
                        ids.add(nodeId);
                        ids.add(relation.fromId());
                        ids.add(relation.toId());
                    }, Set::addAll);
            centeredNodeIds.add(nodeId);
            selectedNodeIds.retainAll(centeredNodeIds);
        }

        List<GraphNodeResponse> nodes = selectedNodeIds.stream()
                .map(nodesById::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(GraphNode::name).thenComparing(GraphNode::id))
                .map(GraphNodeResponse::from)
                .toList();

        Set<String> filteredNodeIds = nodes.stream()
                .map(GraphNodeResponse::id)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<GraphRelation> filteredRelations = snapshot.graph().relations().stream()
                .filter(relation -> filteredNodeIds.contains(relation.fromId()) && filteredNodeIds.contains(relation.toId()))
                .toList();

        Set<String> evidenceIds = filteredRelations.stream()
                .flatMap(relation -> relation.evidenceIds().stream())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<EvidenceResponse> evidences = snapshot.graph().evidences().stream()
                .filter(evidence -> evidenceIds.contains(evidence.id()))
                .map(EvidenceResponse::from)
                .toList();

        List<GraphRelationResponse> relations = filteredRelations.stream()
                .map(relation -> toRelationResponse(relation, nodesById))
                .toList();

        return new GraphResponse(
                snapshot.id(),
                snapshot.rootPath(),
                snapshot.createdAt(),
                nodes.size(),
                relations.size(),
                evidences.size(),
                nodes,
                relations,
                evidences
        );
    }

    public ServiceChainResponse serviceChain(String endpointId) {
        AnalysisSnapshot snapshot = latest();
        Map<String, GraphNode> nodesById = indexNodes(snapshot.graph().nodes());
        if (!nodesById.containsKey(endpointId)) {
            throw new IllegalArgumentException("Node not found: " + endpointId);
        }

        Set<String> visitedNodeIds = new LinkedHashSet<>();
        Set<GraphRelation> collectedRelations = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(endpointId);
        visitedNodeIds.add(endpointId);

        while (!queue.isEmpty()) {
            String currentId = queue.removeFirst();
            snapshot.graph().relations().stream()
                    .filter(relation -> relation.fromId().equals(currentId))
                    .filter(relation -> isServiceChainRelation(relation.relationType()))
                    .forEach(relation -> {
                        collectedRelations.add(relation);
                        if (visitedNodeIds.add(relation.toId())) {
                            queue.addLast(relation.toId());
                        }
                    });
        }

        Set<String> evidenceIds = collectedRelations.stream()
                .flatMap(relation -> relation.evidenceIds().stream())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        GraphResponse response = new GraphResponse(
                snapshot.id(),
                snapshot.rootPath(),
                snapshot.createdAt(),
                visitedNodeIds.size(),
                collectedRelations.size(),
                evidenceIds.size(),
                visitedNodeIds.stream()
                        .map(nodesById::get)
                        .filter(Objects::nonNull)
                        .map(GraphNodeResponse::from)
                        .toList(),
                collectedRelations.stream()
                        .map(relation -> toRelationResponse(relation, nodesById))
                        .toList(),
                snapshot.graph().evidences().stream()
                        .filter(evidence -> evidenceIds.contains(evidence.id()))
                        .map(EvidenceResponse::from)
                        .toList()
        );
        return new ServiceChainResponse(response);
    }

    private boolean matchesType(GraphNode node, String type) {
        if (type == null || type.isBlank()) {
            return true;
        }
        return node.type() == NodeType.valueOf(type.toUpperCase(Locale.ROOT));
    }

    private boolean isServiceChainRelation(com.traceviewcore.domain.RelationType relationType) {
        return relationType == com.traceviewcore.domain.RelationType.HANDLED_BY
                || relationType == com.traceviewcore.domain.RelationType.INVOKES
                || relationType == com.traceviewcore.domain.RelationType.ACCESSES
                || relationType == com.traceviewcore.domain.RelationType.CALLS;
    }

    private Map<String, GraphNode> indexNodes(List<GraphNode> nodes) {
        Map<String, GraphNode> nodesById = new LinkedHashMap<>();
        nodes.forEach(node -> nodesById.put(node.id(), node));
        return nodesById;
    }

    private GraphRelationResponse toRelationResponse(GraphRelation relation, Map<String, GraphNode> nodesById) {
        return GraphRelationResponse.from(
                relation,
                nodesById.get(relation.fromId()),
                nodesById.get(relation.toId())
        );
    }
}
