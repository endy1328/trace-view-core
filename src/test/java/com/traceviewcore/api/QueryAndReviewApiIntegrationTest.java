package com.traceviewcore.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.traceviewcore.bootstrap.TraceViewCoreApplication;
import com.traceviewcore.domain.AnalysisGraph;
import com.traceviewcore.domain.AnalysisSnapshot;
import com.traceviewcore.domain.CertaintyType;
import com.traceviewcore.domain.Evidence;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.GraphRelation;
import com.traceviewcore.domain.NodeType;
import com.traceviewcore.domain.RelationType;
import com.traceviewcore.domain.ReviewStatus;
import com.traceviewcore.persistence.GraphStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(classes = TraceViewCoreApplication.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class QueryAndReviewApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GraphStore graphStore;

    @BeforeEach
    void setUp() {
        graphStore.save(new AnalysisSnapshot(
                "snapshot_api_flow",
                "/workspace/query-review-flow",
                Instant.parse("2026-03-27T05:00:00Z"),
                new AnalysisGraph(
                        List.of(
                                node("endpoint_orders_flow", NodeType.API_ENDPOINT, "GET /orders/{id}"),
                                node("entry_orders_flow", NodeType.BACKEND_ENTRY_POINT, "OrderController#findOrder"),
                                node("service_orders_flow", NodeType.SERVICE_METHOD, "OrderService#findOrder"),
                                node("repository_orders_flow", NodeType.REPOSITORY_CALL, "OrderRepository")
                        ),
                        List.of(
                                relation("rel_flow_1", "endpoint_orders_flow", "entry_orders_flow", RelationType.HANDLED_BY, "ev_flow_1"),
                                relation("rel_flow_2", "entry_orders_flow", "service_orders_flow", RelationType.INVOKES, "ev_flow_2"),
                                relation("rel_flow_3", "service_orders_flow", "repository_orders_flow", RelationType.ACCESSES, "ev_flow_3")
                        ),
                        List.of(
                                evidence("ev_flow_1", "OrderController.java", 20, "OrderController#findOrder"),
                                evidence("ev_flow_2", "OrderController.java", 24, "OrderController#findOrder"),
                                evidence("ev_flow_3", "OrderService.java", 18, "OrderService#findOrder")
                        )
                )
        ));
    }

    @Test
    void servesLatestGraphNodeDetailAndServiceChain() throws Exception {
        mockMvc.perform(get("/api/query/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("snapshot_api_flow")))
                .andExpect(jsonPath("$.graph.nodes", hasSize(4)));

        mockMvc.perform(get("/api/query/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.snapshotId", is("snapshot_api_flow")))
                .andExpect(jsonPath("$.nodes", hasSize(4)))
                .andExpect(jsonPath("$.relations", hasSize(3)));

        mockMvc.perform(get("/api/query/endpoints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("endpoint_orders_flow")));

        mockMvc.perform(get("/api/query/nodes/service_orders_flow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.node.id", is("service_orders_flow")))
                .andExpect(jsonPath("$.incomingRelations", hasSize(1)))
                .andExpect(jsonPath("$.outgoingRelations", hasSize(1)));

        mockMvc.perform(get("/api/query/endpoints/endpoint_orders_flow/service-chain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.graph.nodes", hasSize(4)))
                .andExpect(jsonPath("$.graph.relations", hasSize(3)));
    }

    @Test
    void createsAndApprovesAnnotationThroughApi() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/reviews/annotations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetId":"service_orders_flow","content":"주문 조회 핵심 로직","author":"tester"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andReturn();

        String body = createResult.getResponse().getContentAsString();
        String annotationId = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/reviews/annotations")
                        .param("targetId", "service_orders_flow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].targetId", is("service_orders_flow")));

        mockMvc.perform(post("/api/reviews/annotations/" + annotationId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"approver":"lead"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.approver", is("lead")));
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

    private static Evidence evidence(String id, String sourceFile, Integer sourceLine, String sourceSymbol) {
        return new Evidence(
                id,
                "STATIC_ANALYSIS",
                sourceFile,
                sourceLine,
                sourceSymbol,
                "rule-flow",
                "integration-test",
                "1.0.0",
                Instant.parse("2026-03-27T05:00:00Z")
        );
    }
}
