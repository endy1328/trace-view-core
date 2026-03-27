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
class TraceApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GraphStore graphStore;

    @BeforeEach
    void setUp() {
        graphStore.save(new AnalysisSnapshot(
                "snapshot_trace_api",
                "/workspace/trace-api",
                Instant.parse("2026-03-27T06:00:00Z"),
                new AnalysisGraph(
                        List.of(
                                node("endpoint_trace", NodeType.API_ENDPOINT, "GET /orders/{id}", Map.of("httpMethod", "GET", "path", "/orders/{id}")),
                                node("service_trace", NodeType.SERVICE_METHOD, "OrderService#findOrder", Map.of())
                        ),
                        List.of(
                                relation("rel_trace_1", "endpoint_trace", "service_trace", RelationType.INVOKES, "ev_trace_1")
                        ),
                        List.of(evidence("ev_trace_1"))
                )
        ));
    }

    @Test
    void createsSessionIngestsEventsAndReturnsCorrelation() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/traces/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"traceId":"trace-api-1","environment":"dev"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.eventCount", is(0)))
                .andReturn();

        String sessionId = createResult.getResponse().getContentAsString().replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/traces/sessions/" + sessionId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "events": [
                                    {
                                      "eventType": "API_CALL",
                                      "occurredAt": "2026-03-27T06:01:00Z",
                                      "httpMethod": "GET",
                                      "path": "/orders/{id}",
                                      "traceId": "trace-api-1",
                                      "spanId": "span-1",
                                      "metadata": {}
                                    },
                                    {
                                      "eventType": "SERVICE_SPAN",
                                      "occurredAt": "2026-03-27T06:01:01Z",
                                      "sourceSymbol": "OrderService#findOrder",
                                      "traceId": "trace-api-1",
                                      "spanId": "span-2",
                                      "metadata": {}
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(2)));

        mockMvc.perform(get("/api/traces/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/traces/sessions/" + sessionId + "/correlation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchedNodes", hasSize(2)))
                .andExpect(jsonPath("$.matchedLinks.length()", is(3)));
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
                "TraceApi.java",
                12,
                "traceSymbol",
                "rule-trace-api",
                "trace-api-test",
                "1.0.0",
                Instant.parse("2026-03-27T06:00:00Z")
        );
    }
}
