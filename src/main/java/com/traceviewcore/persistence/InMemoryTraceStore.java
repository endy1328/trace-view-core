package com.traceviewcore.persistence;

import com.traceviewcore.domain.TraceEvent;
import com.traceviewcore.domain.TraceSession;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTraceStore implements TraceStore {

    private final ConcurrentHashMap<String, TraceSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TraceEvent> events = new ConcurrentHashMap<>();

    @Override
    public TraceSession saveSession(TraceSession session) {
        sessions.put(session.id(), session);
        return session;
    }

    @Override
    public Optional<TraceSession> findSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public List<TraceSession> listSessions() {
        return sessions.values().stream()
                .sorted(Comparator.comparing(TraceSession::collectedAt).reversed())
                .toList();
    }

    @Override
    public TraceEvent saveEvent(TraceEvent event) {
        events.put(event.id(), event);
        return event;
    }

    @Override
    public List<TraceEvent> findEventsBySessionId(String sessionId) {
        return events.values().stream()
                .filter(event -> event.sessionId().equals(sessionId))
                .sorted(Comparator.comparing(TraceEvent::occurredAt).thenComparing(TraceEvent::id))
                .toList();
    }
}
