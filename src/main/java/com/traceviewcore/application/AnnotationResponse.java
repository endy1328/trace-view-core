package com.traceviewcore.application;

import com.traceviewcore.domain.Annotation;
import com.traceviewcore.domain.ReviewStatus;
import java.time.Instant;

public record AnnotationResponse(
        String id,
        String targetId,
        String content,
        String author,
        String approver,
        ReviewStatus status,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static AnnotationResponse from(Annotation annotation) {
        return new AnnotationResponse(
                annotation.id(),
                annotation.targetId(),
                annotation.content(),
                annotation.author(),
                annotation.approver(),
                annotation.status(),
                annotation.rejectionReason(),
                annotation.createdAt(),
                annotation.updatedAt()
        );
    }
}
