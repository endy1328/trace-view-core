package com.traceviewcore.persistence;

import com.traceviewcore.domain.AnalysisSnapshot;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.GraphRelation;
import java.util.List;
import java.util.Optional;

public interface GraphStore {

    void save(AnalysisSnapshot snapshot);

    Optional<AnalysisSnapshot> latest();

    List<GraphNode> search(String query);

    List<GraphNode> findByType(String type);

    Optional<GraphNode> findNode(String id);

    List<GraphRelation> findRelationsForNode(String id);
}
