package com.traceviewcore.application;

import java.util.List;

public record TraceSessionDetailResponse(
        TraceSessionResponse session,
        List<TraceEventResponse> events
) {
}
