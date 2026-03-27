package com.traceviewcore.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.traceviewcore.domain.AnalysisGraph;
import com.traceviewcore.domain.AnalysisSnapshot;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.NodeType;
import com.traceviewcore.domain.ReviewStatus;
import com.traceviewcore.persistence.InMemoryAnnotationStore;
import com.traceviewcore.persistence.InMemoryGraphStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewApplicationServiceTest {

    private ReviewApplicationService reviewApplicationService;

    @BeforeEach
    void setUp() {
        InMemoryGraphStore graphStore = new InMemoryGraphStore();
        graphStore.save(new AnalysisSnapshot(
                "snapshot_1",
                "/workspace/sample",
                Instant.parse("2026-03-26T08:00:00Z"),
                new AnalysisGraph(
                        List.of(node("service_order", NodeType.SERVICE_METHOD, "OrderService.find")),
                        List.of(),
                        List.of()
                )
        ));
        reviewApplicationService = new ReviewApplicationService(new InMemoryAnnotationStore(), graphStore);
    }

    @Test
    void createsAndApprovesAnnotation() {
        AnnotationResponse draft = reviewApplicationService.createDraft(
                new AnnotationCreateRequest("service_order", "주문 조회 핵심 로직", "alice")
        );

        assertEquals(ReviewStatus.DRAFT, draft.status());
        assertEquals(1, reviewApplicationService.pending().size());

        AnnotationResponse approved = reviewApplicationService.approve(
                draft.id(),
                new AnnotationDecisionRequest("lead", null)
        );

        assertEquals(ReviewStatus.APPROVED, approved.status());
        assertEquals("lead", approved.approver());
        assertEquals(0, reviewApplicationService.pending().size());
    }

    @Test
    void rejectsAnnotationWithReason() {
        AnnotationResponse draft = reviewApplicationService.createDraft(
                new AnnotationCreateRequest("service_order", "불명확한 설명", "alice")
        );

        AnnotationResponse rejected = reviewApplicationService.reject(
                draft.id(),
                new AnnotationDecisionRequest("lead", "근거 부족")
        );

        assertEquals(ReviewStatus.REJECTED, rejected.status());
        assertEquals("근거 부족", rejected.rejectionReason());
        assertEquals(1, reviewApplicationService.listByTarget("service_order").size());
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
}
