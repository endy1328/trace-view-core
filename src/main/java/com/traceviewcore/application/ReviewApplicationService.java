package com.traceviewcore.application;

import com.traceviewcore.common.IdGenerator;
import com.traceviewcore.domain.Annotation;
import com.traceviewcore.domain.ReviewStatus;
import com.traceviewcore.persistence.AnnotationStore;
import com.traceviewcore.persistence.GraphStore;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReviewApplicationService {

    private final AnnotationStore annotationStore;
    private final GraphStore graphStore;

    public ReviewApplicationService(AnnotationStore annotationStore, GraphStore graphStore) {
        this.annotationStore = annotationStore;
        this.graphStore = graphStore;
    }

    public AnnotationResponse createDraft(AnnotationCreateRequest request) {
        assertTargetExists(request.targetId());
        Instant now = Instant.now();
        Annotation annotation = new Annotation(
                IdGenerator.newId("annotation"),
                request.targetId(),
                request.content(),
                request.author(),
                null,
                ReviewStatus.DRAFT,
                null,
                now,
                now
        );
        return AnnotationResponse.from(annotationStore.save(annotation));
    }

    public List<AnnotationResponse> listByTarget(String targetId) {
        return annotationStore.findByTargetId(targetId).stream()
                .map(AnnotationResponse::from)
                .toList();
    }

    public List<AnnotationResponse> pending() {
        return annotationStore.findPending().stream()
                .map(AnnotationResponse::from)
                .toList();
    }

    public AnnotationResponse approve(String annotationId, AnnotationDecisionRequest request) {
        Annotation annotation = annotationStore.findById(annotationId)
                .orElseThrow(() -> new IllegalArgumentException("Annotation not found: " + annotationId));
        Annotation updated = new Annotation(
                annotation.id(),
                annotation.targetId(),
                annotation.content(),
                annotation.author(),
                request.approver(),
                ReviewStatus.APPROVED,
                null,
                annotation.createdAt(),
                Instant.now()
        );
        return AnnotationResponse.from(annotationStore.save(updated));
    }

    public AnnotationResponse reject(String annotationId, AnnotationDecisionRequest request) {
        Annotation annotation = annotationStore.findById(annotationId)
                .orElseThrow(() -> new IllegalArgumentException("Annotation not found: " + annotationId));
        Annotation updated = new Annotation(
                annotation.id(),
                annotation.targetId(),
                annotation.content(),
                annotation.author(),
                request.approver(),
                ReviewStatus.REJECTED,
                request.reason(),
                annotation.createdAt(),
                Instant.now()
        );
        return AnnotationResponse.from(annotationStore.save(updated));
    }

    private void assertTargetExists(String targetId) {
        graphStore.findNode(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target node not found: " + targetId));
    }
}
