package com.traceviewcore.analysis.adapter;

import org.springframework.stereotype.Component;

@Component
public class AStoreBatchLegacyAnalysisAdapter extends AStoreLegacyAnalysisAdapter {

    public AStoreBatchLegacyAnalysisAdapter(
            com.traceviewcore.analysis.SpringSourceParser parser,
            com.traceviewcore.analysis.evidence.EvidenceFactory evidenceFactory
    ) {
        super(parser, evidenceFactory);
    }

    @Override
    public String id() {
        return "astore-batch-legacy";
    }
}
