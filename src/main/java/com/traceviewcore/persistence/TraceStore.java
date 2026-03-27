package com.traceviewcore.persistence;

import com.traceviewcore.domain.TraceEvent;
import com.traceviewcore.domain.TraceSession;
import java.util.List;
import java.util.Optional;

public interface TraceStore {

    TraceSession saveSession(TraceSession session);

    Optional<TraceSession> findSession(String sessionId);

    List<TraceSession> listSessions();

    TraceEvent saveEvent(TraceEvent event);

    List<TraceEvent> findEventsBySessionId(String sessionId);
}
