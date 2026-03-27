package com.traceviewcore.application;

import jakarta.validation.constraints.NotBlank;

public record AnnotationCreateRequest(
        @NotBlank String targetId,
        @NotBlank String content,
        @NotBlank String author
) {
}
