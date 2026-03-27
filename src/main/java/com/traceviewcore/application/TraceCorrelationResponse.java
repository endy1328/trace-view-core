package com.traceviewcore.application;

import java.util.List;

public record TraceCorrelationResponse(
        TraceSessionResponse session,
        List<TraceEventResponse> events,
        List<TraceCorrelationNodeResponse> matchedNodes,
        List<TraceCorrelationLinkResponse> matchedLinks,
        List<TraceEventResponse> unmatchedEvents
) {
}
