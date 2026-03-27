package com.traceviewcore.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.traceviewcore.domain.AnalysisGraph;
import com.traceviewcore.domain.AnalysisSnapshot;
import com.traceviewcore.domain.CertaintyType;
import com.traceviewcore.domain.Evidence;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.GraphRelation;
import com.traceviewcore.domain.NodeType;
import com.traceviewcore.domain.RelationType;
import com.traceviewcore.domain.ReviewStatus;
import com.traceviewcore.persistence.InMemoryAnnotationStore;
import com.traceviewcore.persistence.InMemoryGraphStore;
import java.time.temporal.ChronoUnit;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryApplicationServiceTest {

    private QueryApplicationService queryApplicationService;

    @BeforeEach
    void setUp() {
        InMemoryGraphStore graphStore = new InMemoryGraphStore();
        InMemoryAnnotationStore annotationStore = new InMemoryAnnotationStore();
        graphStore.save(new AnalysisSnapshot(
                "snapshot_1",
                "/workspace/sample",
                Instant.parse("2026-03-26T08:00:00Z"),
                new AnalysisGraph(
                        List.of(
                                node("endpoint_orders", NodeType.API_ENDPOINT, "GET /orders/{id}"),
                                node("entry_order", NodeType.BACKEND_ENTRY_POINT, "OrderController.find"),
                                node("service_order", NodeType.SERVICE_METHOD, "OrderService.find"),
                                node("repository_order", NodeType.REPOSITORY_CALL, "OrderRepository")
                        ),
                        List.of(
                                relation("rel_1", "endpoint_orders", "entry_order", RelationType.HANDLED_BY, List.of("ev_1")),
                                relation("rel_2", "entry_order", "service_order", RelationType.INVOKES, List.of("ev_2")),
                                relation("rel_3", "service_order", "repository_order", RelationType.ACCESSES, List.of("ev_3"))
                        ),
                        List.of(
                                evidence("ev_1", "OrderController.java", 10),
                                evidence("ev_2", "OrderController.java", 25),
                                evidence("ev_3", "OrderService.java", 18)
                        )
                )
        ));
        annotationStore.save(new com.traceviewcore.domain.Annotation(
                "annotation_1",
                "service_order",
                "주문 조회 핵심 로직",
                "alice",
                null,
                ReviewStatus.DRAFT,
                null,
                Instant.parse("2026-03-26T08:00:00Z"),
                Instant.parse("2026-03-26T08:00:00Z").plus(5, ChronoUnit.MINUTES)
        ));
        queryApplicationService = new QueryApplicationService(graphStore, annotationStore);
    }

    @Test
    void returnsGraphFilteredByNodeId() {
        GraphResponse response = queryApplicationService.graph("entry_order", null);

        assertEquals("snapshot_1", response.snapshotId());
        assertEquals(3, response.nodeCount());
        assertEquals(2, response.relationCount());
        assertEquals(2, response.evidenceCount());
        assertIterableEquals(
                List.of("endpoint_orders", "entry_order", "service_order"),
                response.nodes().stream().map(GraphNodeResponse::id).toList()
        );
    }

    @Test
    void returnsGraphFilteredByType() {
        GraphResponse response = queryApplicationService.graph(null, "service_method");

        assertEquals(1, response.nodeCount());
        assertEquals(0, response.relationCount());
        assertEquals("service_order", response.nodes().get(0).id());
    }

    @Test
    void returnsNodeDetailWithIncomingAndOutgoingRelations() {
        NodeDetailResponse response = queryApplicationService.node("service_order");

        assertEquals("service_order", response.node().id());
        assertEquals(1, response.incomingRelations().size());
        assertEquals("entry_order", response.incomingRelations().get(0).connectedNode().id());
        assertEquals(RelationType.INVOKES, response.incomingRelations().get(0).relation().relationType());

        assertEquals(1, response.outgoingRelations().size());
        assertEquals("repository_order", response.outgoingRelations().get(0).connectedNode().id());
        assertEquals(RelationType.ACCESSES, response.outgoingRelations().get(0).relation().relationType());

        assertIterableEquals(
                List.of("ev_2", "ev_3"),
                response.evidences().stream().map(EvidenceResponse::id).toList()
        );
        assertEquals(1, response.annotations().size());
        assertEquals("annotation_1", response.annotations().get(0).id());
    }

    @Test
    void returnsServiceChainFromEndpoint() {
        ServiceChainResponse response = queryApplicationService.serviceChain("endpoint_orders");

        assertEquals(4, response.graph().nodeCount());
        assertEquals(3, response.graph().relationCount());
        assertIterableEquals(
                List.of("endpoint_orders", "entry_order", "service_order", "repository_order"),
                response.graph().nodes().stream().map(GraphNodeResponse::id).toList()
        );
    }

    @Test
    void returnsLatestOptionalWhenSnapshotExists() {
        assertTrue(queryApplicationService.latestOptional().isPresent());
        assertEquals("snapshot_1", queryApplicationService.latestOptional().orElseThrow().id());
    }

    private static GraphNode node(String id, NodeType type, String name) {
        return new GraphNode(
                id,
                type,
                name,
                "src/main/java/sample/" + name + ".java",
                name,
                1.0,
                ReviewStatus.DRAFT,
                Set.of(type.name().toLowerCase()),
                Map.of("name", name)
        );
    }

    private static GraphRelation relation(String id, String fromId, String toId, RelationType type, List<String> evidenceIds) {
        return new GraphRelation(
                id,
                fromId,
                toId,
                type,
                CertaintyType.CONFIRMED,
                1.0,
                evidenceIds
        );
    }

    private static Evidence evidence(String id, String sourceFile, Integer sourceLine) {
        return new Evidence(
                id,
                "STATIC_ANALYSIS",
                sourceFile,
                sourceLine,
                "sampleSymbol",
                "rule-1",
                "test-analyzer",
                "1.0.0",
                Instant.parse("2026-03-26T08:00:00Z")
        );
    }
}
