package com.traceviewcore.application;

import java.util.List;

public record ModuleDescriptorResponse(
        String name,
        String path,
        String moduleType,
        String recommendedAdapterId,
        List<String> reasons
) {
}
