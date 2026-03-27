package com.traceviewcore.api;

import com.traceviewcore.application.AnalysisOrchestrator;
import com.traceviewcore.application.AnalysisRunRequest;
import com.traceviewcore.application.AnalysisRunResponse;
import com.traceviewcore.application.ModuleClassificationRequest;
import com.traceviewcore.application.ModuleClassificationResponse;
import com.traceviewcore.application.ModuleClassificationService;
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
    private final ModuleClassificationService moduleClassificationService;

    public AnalysisController(
            AnalysisOrchestrator analysisOrchestrator,
            ModuleClassificationService moduleClassificationService
    ) {
        this.analysisOrchestrator = analysisOrchestrator;
        this.moduleClassificationService = moduleClassificationService;
    }

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AnalysisRunResponse run(@Valid @RequestBody AnalysisRunRequest request) throws IOException {
        return analysisOrchestrator.run(request.rootPath(), request.adapterId());
    }

    @PostMapping("/classify-modules")
    public ModuleClassificationResponse classifyModules(@Valid @RequestBody ModuleClassificationRequest request) throws IOException {
        return moduleClassificationService.classify(request.rootPath());
    }
}
