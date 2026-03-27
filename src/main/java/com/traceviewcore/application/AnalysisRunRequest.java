package com.traceviewcore.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisRunRequest(
        @NotBlank String rootPath,
        String adapterId
) {
}
