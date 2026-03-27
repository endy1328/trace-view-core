package com.traceviewcore.application;

import com.traceviewcore.common.IdGenerator;
import com.traceviewcore.domain.AnalysisSnapshot;
import com.traceviewcore.domain.GraphNode;
import com.traceviewcore.domain.NodeType;
import com.traceviewcore.domain.TraceEvent;
import com.traceviewcore.domain.TraceEventType;
import com.traceviewcore.domain.TraceSession;
import com.traceviewcore.persistence.GraphStore;
import com.traceviewcore.persistence.TraceStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class TraceApplicationService {

    private final TraceStore traceStore;
    private final GraphStore graphStore;

    public TraceApplicationService(TraceStore traceStore, GraphStore graphStore) {
        this.traceStore = traceStore;
        this.graphStore = graphStore;
    }

    public TraceSessionResponse createSession(TraceSessionCreateRequest request) {
        TraceSession session = new TraceSession(
                IdGenerator.newId("trace_session"),
                request.traceId(),
                request.environment(),
                Instant.now()
        );
        traceStore.saveSession(session);
        return TraceSessionResponse.from(session, 0);
    }

    public List<TraceSessionResponse> sessions() {
        return traceStore.listSessions().stream()
                .map(session -> TraceSessionResponse.from(session, traceStore.findEventsBySessionId(session.id()).size()))
                .toList();
    }

    public TraceSessionDetailResponse session(String sessionId) {
        TraceSession session = traceStore.findSession(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Trace session not found: " + sessionId));
        List<TraceEventResponse> events = traceStore.findEventsBySessionId(sessionId).stream()
                .map(TraceEventResponse::from)
                .toList();
        return new TraceSessionDetailResponse(
                TraceSessionResponse.from(session, events.size()),
                events
        );
    }

    public TraceSessionDetailResponse ingestEvents(String sessionId, TraceEventIngestRequest request) {
        TraceSession session = traceStore.findSession(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Trace session not found: " + sessionId));

        request.events().forEach(event -> traceStore.saveEvent(new TraceEvent(
                IdGenerator.newId("trace_event"),
                sessionId,
                event.eventType(),
                event.occurredAt(),
                event.screenName(),
                event.httpMethod(),
                event.path(),
                event.sourceSymbol(),
                event.traceId(),
                event.spanId(),
                event.metadata() == null ? Map.of() : Map.copyOf(event.metadata())
        )));

        return session(session.id());
    }

    public TraceCorrelationResponse correlation(String sessionId) {
        TraceSessionDetailResponse detail = session(sessionId);
        Optional<AnalysisSnapshot> snapshotOptional = graphStore.latest();
        if (snapshotOptional.isEmpty()) {
            return new TraceCorrelationResponse(
                    detail.session(),
                    detail.events(),
                    List.of(),
                    List.of(),
                    detail.events()
            );
        }

        AnalysisSnapshot snapshot = snapshotOptional.orElseThrow();
        Map<String, GraphNode> nodesById = snapshot.graph().nodes().stream()
                .collect(LinkedHashMap::new, (map, node) -> map.put(node.id(), node), Map::putAll);

        List<TraceCorrelationNodeResponse> matchedNodes = new ArrayList<>();
        List<TraceEventResponse> unmatchedEvents = new ArrayList<>();
        List<MatchedTraceNode> matchedTraceNodes = new ArrayList<>();

        for (TraceEventResponse event : detail.events()) {
            Optional<MatchedTraceNode> matched = matchNode(event, snapshot.graph().nodes());
            if (matched.isPresent()) {
                MatchedTraceNode traceNode = matched.orElseThrow();
                matchedTraceNodes.add(traceNode);
                matchedNodes.add(new TraceCorrelationNodeResponse(
                        event.id(),
                        traceNode.nodeId(),
                        traceNode.nodeType(),
                        nodesById.get(traceNode.nodeId()).name(),
                        traceNode.matchReason()
                ));
            } else {
                unmatchedEvents.add(event);
            }
        }

        List<TraceCorrelationLinkResponse> matchedLinks = buildLinks(detail.session().id(), matchedTraceNodes);

        return new TraceCorrelationResponse(
                detail.session(),
                detail.events(),
                matchedNodes,
                matchedLinks,
                unmatchedEvents
        );
    }

    private Optional<MatchedTraceNode> matchNode(TraceEventResponse event, List<GraphNode> nodes) {
        if (event.eventType() == TraceEventType.API_CALL) {
            return nodes.stream()
                    .filter(node -> node.type() == NodeType.API_ENDPOINT)
                    .filter(node -> equalsIgnoreCase(node.metadata().get("httpMethod"), event.httpMethod()))
                    .filter(node -> Objects.equals(node.metadata().get("path"), event.path()))
                    .findFirst()
                    .map(node -> new MatchedTraceNode(event.id(), node.id(), node.type(), "api_call_path_match", 1.0));
        }

        if (event.eventType() == TraceEventType.BACKEND_ENTRY) {
            return nodes.stream()
                    .filter(node -> node.type() == NodeType.BACKEND_ENTRY_POINT)
                    .filter(node -> symbolMatches(node, event.sourceSymbol()))
                    .findFirst()
                    .map(node -> new MatchedTraceNode(event.id(), node.id(), node.type(), "backend_entry_symbol_match", 0.95));
        }

        if (event.eventType() == TraceEventType.SERVICE_SPAN) {
            return nodes.stream()
                    .filter(node -> node.type() == NodeType.SERVICE_METHOD)
                    .filter(node -> symbolMatches(node, event.sourceSymbol()))
                    .findFirst()
                    .map(node -> new MatchedTraceNode(event.id(), node.id(), node.type(), "service_span_symbol_match", 0.95));
        }

        if (event.eventType() == TraceEventType.SCREEN_VIEW) {
            return nodes.stream()
                    .filter(node -> node.type() == NodeType.SCREEN)
                    .filter(node -> equalsIgnoreCase(node.name(), event.screenName()))
                    .findFirst()
                    .map(node -> new MatchedTraceNode(event.id(), node.id(), node.type(), "screen_name_match", 0.8));
        }

        return Optional.empty();
    }

    private List<TraceCorrelationLinkResponse> buildLinks(String sessionId, List<MatchedTraceNode> matchedTraceNodes) {
        List<TraceCorrelationLinkResponse> links = new ArrayList<>();
        Set<String> linkKeys = new LinkedHashSet<>();

        for (MatchedTraceNode matchedNode : matchedTraceNodes) {
            addLink(links, linkKeys, "trace_session:" + sessionId, matchedNode.nodeId(), matchedNode.confidence(), List.of(matchedNode.eventId()));
        }

        for (int index = 0; index < matchedTraceNodes.size() - 1; index++) {
            MatchedTraceNode current = matchedTraceNodes.get(index);
            MatchedTraceNode next = matchedTraceNodes.get(index + 1);
            if (!current.nodeId().equals(next.nodeId())) {
                addLink(
                        links,
                        linkKeys,
                        current.nodeId(),
                        next.nodeId(),
                        Math.min(current.confidence(), next.confidence()),
                        List.of(current.eventId(), next.eventId())
                );
            }
        }

        return links;
    }

    private void addLink(
            List<TraceCorrelationLinkResponse> links,
            Set<String> linkKeys,
            String fromRef,
            String toRef,
            double confidence,
            List<String> eventIds
    ) {
        String key = fromRef + "|" + toRef;
        if (linkKeys.add(key)) {
            links.add(new TraceCorrelationLinkResponse(fromRef, toRef, "TRACED_TO", confidence, eventIds));
        }
    }

    private boolean symbolMatches(GraphNode node, String sourceSymbol) {
        if (sourceSymbol == null || sourceSymbol.isBlank()) {
            return false;
        }
        return equalsIgnoreCase(node.sourceSymbol(), sourceSymbol) || equalsIgnoreCase(node.name(), sourceSymbol);
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private record MatchedTraceNode(
            String eventId,
            String nodeId,
            NodeType nodeType,
            String matchReason,
            double confidence
    ) {
    }
}
