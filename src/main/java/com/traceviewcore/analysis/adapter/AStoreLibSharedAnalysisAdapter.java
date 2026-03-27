package com.traceviewcore.analysis.adapter;

import org.springframework.stereotype.Component;

@Component
public class AStoreLibSharedAnalysisAdapter extends AStoreLegacyAnalysisAdapter {

    public AStoreLibSharedAnalysisAdapter(
            com.traceviewcore.analysis.SpringSourceParser parser,
            com.traceviewcore.analysis.evidence.EvidenceFactory evidenceFactory
    ) {
        super(parser, evidenceFactory);
    }

    @Override
    public String id() {
        return "astore-lib-shared";
    }
}
