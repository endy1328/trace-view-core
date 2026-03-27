package com.traceviewcore.api;

import com.traceviewcore.application.TraceApplicationService;
import com.traceviewcore.application.TraceCorrelationResponse;
import com.traceviewcore.application.TraceEventIngestRequest;
import com.traceviewcore.application.TraceSessionCreateRequest;
import com.traceviewcore.application.TraceSessionDetailResponse;
import com.traceviewcore.application.TraceSessionResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/traces")
public class TraceController {

    private final TraceApplicationService traceApplicationService;

    public TraceController(TraceApplicationService traceApplicationService) {
        this.traceApplicationService = traceApplicationService;
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public TraceSessionResponse createSession(@Valid @RequestBody TraceSessionCreateRequest request) {
        return traceApplicationService.createSession(request);
    }

    @GetMapping("/sessions")
    public List<TraceSessionResponse> sessions() {
        return traceApplicationService.sessions();
    }

    @GetMapping("/sessions/{id}")
    public TraceSessionDetailResponse session(@PathVariable String id) {
        return traceApplicationService.session(id);
    }

    @PostMapping("/sessions/{id}/events")
    public TraceSessionDetailResponse ingestEvents(
            @PathVariable String id,
            @Valid @RequestBody TraceEventIngestRequest request
    ) {
        return traceApplicationService.ingestEvents(id, request);
    }

    @GetMapping("/sessions/{id}/correlation")
    public TraceCorrelationResponse correlation(@PathVariable String id) {
        return traceApplicationService.correlation(id);
    }
}
