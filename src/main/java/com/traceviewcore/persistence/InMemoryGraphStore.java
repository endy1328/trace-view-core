package com.traceviewcore.persistence;

import com.traceviewcore.domain.AnalysisSnapshot;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.GraphRelation;
import com.traceviewcore.domain.NodeType;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryGraphStore implements GraphStore {

    private final AtomicReference<AnalysisSnapshot> latestSnapshot = new AtomicReference<>();

    @Override
    public void save(AnalysisSnapshot snapshot) {
        latestSnapshot.set(snapshot);
    }

    @Override
    public Optional<AnalysisSnapshot> latest() {
        return Optional.ofNullable(latestSnapshot.get());
    }

    @Override
    public List<GraphNode> search(String query) {
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        return latest().map(snapshot -> snapshot.graph().nodes().stream()
                        .filter(node -> node.name().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                                || node.metadata().values().stream()
                                .anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(normalizedQuery)))
                        .toList())
                .orElse(List.of());
    }

    @Override
    public List<GraphNode> findByType(String type) {
        NodeType nodeType = NodeType.valueOf(type.toUpperCase(Locale.ROOT));
        return latest().map(snapshot -> snapshot.graph().nodes().stream()
                        .filter(node -> node.type() == nodeType)
                        .toList())
                .orElse(List.of());
    }

    @Override
    public Optional<GraphNode> findNode(String id) {
        return latest().flatMap(snapshot -> snapshot.graph().nodes().stream()
                .filter(node -> node.id().equals(id))
                .findFirst());
    }

    @Override
    public List<GraphRelation> findRelationsForNode(String id) {
        return latest().map(snapshot -> snapshot.graph().relations().stream()
                        .filter(relation -> relation.fromId().equals(id) || relation.toId().equals(id))
                        .toList())
                .orElse(List.of());
    }
}
