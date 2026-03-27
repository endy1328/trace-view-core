package com.traceviewcore.analysis;

import com.traceviewcore.domain.AnalysisGraph;
import com.traceviewcore.domain.Evidence;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.GraphRelation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MutableAnalysisGraph {

    private final List<GraphNode> nodes = new ArrayList<>();
    private final List<GraphRelation> relations = new ArrayList<>();
    private final List<Evidence> evidences = new ArrayList<>();
    private final Set<String> relationKeys = new HashSet<>();

    public void addNode(GraphNode node) {
        if (findNode(node.id()).isEmpty()) {
            nodes.add(node);
        }
    }

    public void addRelation(GraphRelation relation) {
        String relationKey = relation.fromId() + "|" + relation.toId() + "|" + relation.relationType();
        if (relationKeys.add(relationKey)) {
            relations.add(relation);
        }
    }

    public void addEvidence(Evidence evidence) {
        evidences.add(evidence);
    }

    public Optional<GraphNode> findNode(String id) {
        return nodes.stream().filter(node -> node.id().equals(id)).findFirst();
    }

    public List<GraphNode> nodes() {
        return nodes;
    }

    public List<GraphRelation> relations() {
        return relations;
    }

    public AnalysisGraph toImmutableGraph() {
        return new AnalysisGraph(List.copyOf(nodes), List.copyOf(relations), List.copyOf(evidences));
    }
}
