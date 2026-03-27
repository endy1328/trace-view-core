package com.traceviewcore.application;

import java.time.Instant;
import java.util.List;

public record ModuleClassificationResponse(
        String rootPath,
        Instant analyzedAt,
        int moduleCount,
        List<ModuleDescriptorResponse> modules
) {
}
