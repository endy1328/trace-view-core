package com.traceviewcore.api;

import com.traceviewcore.application.AnnotationCreateRequest;
import com.traceviewcore.application.AnnotationDecisionRequest;
import com.traceviewcore.application.AnnotationResponse;
import com.traceviewcore.application.ReviewApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewApplicationService reviewApplicationService;

    public ReviewController(ReviewApplicationService reviewApplicationService) {
        this.reviewApplicationService = reviewApplicationService;
    }

    @PostMapping("/annotations")
    @ResponseStatus(HttpStatus.CREATED)
    public AnnotationResponse createAnnotation(@Valid @RequestBody AnnotationCreateRequest request) {
        return reviewApplicationService.createDraft(request);
    }

    @GetMapping("/annotations")
    public List<AnnotationResponse> annotations(@RequestParam(value = "targetId", required = false) String targetId) {
        if (targetId == null || targetId.isBlank()) {
            return reviewApplicationService.pending();
        }
        return reviewApplicationService.listByTarget(targetId);
    }

    @PostMapping("/annotations/{id}/approve")
    public AnnotationResponse approve(
            @PathVariable String id,
            @Valid @RequestBody AnnotationDecisionRequest request
    ) {
        return reviewApplicationService.approve(id, request);
    }

    @PostMapping("/annotations/{id}/reject")
    public AnnotationResponse reject(
            @PathVariable String id,
            @Valid @RequestBody AnnotationDecisionRequest request
    ) {
        return reviewApplicationService.reject(id, request);
    }
}
