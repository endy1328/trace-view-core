package com.traceviewcore.analysis.adapter;

import org.springframework.stereotype.Component;

@Component
public class AStoreWebMvcAnalysisAdapter extends AStoreLegacyAnalysisAdapter {

    public AStoreWebMvcAnalysisAdapter(
            com.traceviewcore.analysis.SpringSourceParser parser,
            com.traceviewcore.analysis.evidence.EvidenceFactory evidenceFactory
    ) {
        super(parser, evidenceFactory);
    }

    @Override
    public String id() {
        return "astore-web-mvc";
    }
}
