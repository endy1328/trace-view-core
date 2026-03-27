package com.traceviewcore.persistence;

import com.traceviewcore.domain.Annotation;
import java.util.List;
import java.util.Optional;

public interface AnnotationStore {

    Annotation save(Annotation annotation);

    Optional<Annotation> findById(String id);

    List<Annotation> findByTargetId(String targetId);

    List<Annotation> findPending();
}
