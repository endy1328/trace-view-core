package com.traceviewcore.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.traceviewcore.domain.TraceEventType;
import com.traceviewcore.persistence.InMemoryGraphStore;
import com.traceviewcore.persistence.InMemoryTraceStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TraceApplicationServiceTest {

    private TraceApplicationService traceApplicationService;

    @BeforeEach
    void setUp() {
        InMemoryGraphStore graphStore = new InMemoryGraphStore();
        graphStore.save(new AnalysisSnapshot(
                "snapshot_trace",
                "/workspace/trace",
                Instant.parse("2026-03-27T05:00:00Z"),
                new AnalysisGraph(
                        List.of(
                                node("endpoint_orders", NodeType.API_ENDPOINT, "GET /orders/{id}", Map.of("httpMethod", "GET", "path", "/orders/{id}")),
                                node("entry_order", NodeType.BACKEND_ENTRY_POINT, "OrderController#findOrder", Map.of()),
                                node("service_order", NodeType.SERVICE_METHOD, "OrderService#findOrder", Map.of())
                        ),
                        List.of(
                                relation("rel_1", "endpoint_orders", "entry_order", RelationType.HANDLED_BY, "ev_1"),
                                relation("rel_2", "entry_order", "service_order", RelationType.INVOKES, "ev_2")
                        ),
                        List.of(
                                evidence("ev_1"),
                                evidence("ev_2")
                        )
                )
        ));
        traceApplicationService = new TraceApplicationService(new InMemoryTraceStore(), graphStore);
    }

    @Test
    void createsSessionIngestsEventsAndCorrelatesToKnownNodes() {
        TraceSessionResponse session = traceApplicationService.createSession(
                new TraceSessionCreateRequest("trace-1", "dev")
        );

        TraceSessionDetailResponse detail = traceApplicationService.ingestEvents(
                session.id(),
                new TraceEventIngestRequest(List.of(
                        new TraceEventCreateRequest(
                                TraceEventType.API_CALL,
                                Instant.parse("2026-03-27T05:01:00Z"),
                                null,
                                "GET",
                                "/orders/{id}",
                                null,
                                "trace-1",
                                "span-1",
                                Map.of()
                        ),
                        new TraceEventCreateRequest(
                                TraceEventType.SERVICE_SPAN,
                                Instant.parse("2026-03-27T05:01:01Z"),
                                null,
                                null,
                                null,
                                "OrderService#findOrder",
                                "trace-1",
                                "span-2",
                                Map.of()
                        )
                ))
        );

        assertEquals(2, detail.events().size());

        TraceCorrelationResponse correlation = traceApplicationService.correlation(session.id());

        assertEquals(2, correlation.matchedNodes().size());
        assertTrue(correlation.matchedNodes().stream().anyMatch(node -> node.nodeId().equals("endpoint_orders")));
        assertTrue(correlation.matchedNodes().stream().anyMatch(node -> node.nodeId().equals("service_order")));
        assertTrue(correlation.matchedLinks().stream().anyMatch(link -> link.fromRef().startsWith("trace_session:")));
        assertTrue(correlation.matchedLinks().stream().anyMatch(link -> link.fromRef().equals("endpoint_orders") && link.toRef().equals("service_order")));
    }

    private static GraphNode node(String id, NodeType type, String name, Map<String, String> metadata) {
        return new GraphNode(
                id,
                type,
                name,
                "src/main/java/sample/" + name + ".java",
                name,
                1.0,
                ReviewStatus.DRAFT,
                Set.of(type.name().toLowerCase()),
                metadata
        );
    }

    private static GraphRelation relation(String id, String fromId, String toId, RelationType type, String evidenceId) {
        return new GraphRelation(
                id,
                fromId,
                toId,
                type,
                CertaintyType.CONFIRMED,
                1.0,
                List.of(evidenceId)
        );
    }

    private static Evidence evidence(String id) {
        return new Evidence(
                id,
                "STATIC_ANALYSIS",
                "Sample.java",
                10,
                "sampleSymbol",
                "rule-trace",
                "trace-test",
                "1.0.0",
                Instant.parse("2026-03-27T05:00:00Z")
        );
    }
}
