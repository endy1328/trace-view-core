package com.traceviewcore.application;

import jakarta.validation.constraints.NotBlank;

public record AnnotationDecisionRequest(
        @NotBlank String approver,
        String reason
) {
}
