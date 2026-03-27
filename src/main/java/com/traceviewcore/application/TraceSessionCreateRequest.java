package com.traceviewcore.application;

import jakarta.validation.constraints.NotBlank;

public record TraceSessionCreateRequest(
        @NotBlank String traceId,
        @NotBlank String environment
) {
}
