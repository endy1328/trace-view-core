package com.traceviewcore.application;

import jakarta.validation.constraints.NotBlank;

public record AnalysisRunRequest(
        @NotBlank String rootPath
) {
}
