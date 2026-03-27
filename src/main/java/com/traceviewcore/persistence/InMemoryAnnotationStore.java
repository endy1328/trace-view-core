package com.traceviewcore.persistence;

import com.traceviewcore.domain.Annotation;
import com.traceviewcore.domain.ReviewStatus;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAnnotationStore implements AnnotationStore {

    private final ConcurrentHashMap<String, Annotation> annotations = new ConcurrentHashMap<>();

    @Override
    public Annotation save(Annotation annotation) {
        annotations.put(annotation.id(), annotation);
        return annotation;
    }

    @Override
    public Optional<Annotation> findById(String id) {
        return Optional.ofNullable(annotations.get(id));
    }

    @Override
    public List<Annotation> findByTargetId(String targetId) {
        return annotations.values().stream()
                .filter(annotation -> annotation.targetId().equals(targetId))
                .sorted(Comparator.comparing(Annotation::createdAt))
                .toList();
    }

    @Override
    public List<Annotation> findPending() {
        return annotations.values().stream()
                .filter(annotation -> annotation.status() == ReviewStatus.DRAFT)
                .sorted(Comparator.comparing(Annotation::createdAt))
                .toList();
    }
}
