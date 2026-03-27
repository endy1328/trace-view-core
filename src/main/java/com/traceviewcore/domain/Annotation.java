package com.traceviewcore.domain;

import java.time.Instant;

public record Annotation(
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
}
