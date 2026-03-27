package com.traceviewcore.api;

import com.traceviewcore.application.AnalysisOrchestrator;
import com.traceviewcore.application.AnalysisRunRequest;
import com.traceviewcore.application.AnalysisRunResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisOrchestrator analysisOrchestrator;

    public AnalysisController(AnalysisOrchestrator analysisOrchestrator) {
        this.analysisOrchestrator = analysisOrchestrator;
    }

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AnalysisRunResponse run(@Valid @RequestBody AnalysisRunRequest request) throws IOException {
        return analysisOrchestrator.run(request.rootPath());
    }
}
