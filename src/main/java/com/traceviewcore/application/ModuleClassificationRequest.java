package com.traceviewcore.application;

import jakarta.validation.constraints.NotBlank;

public record ModuleClassificationRequest(
        @NotBlank String rootPath
) {
}
