package com.traceviewcore.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TraceEventIngestRequest(
        @NotEmpty List<@Valid TraceEventCreateRequest> events
) {
}
