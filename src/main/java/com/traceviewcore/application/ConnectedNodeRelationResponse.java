package com.traceviewcore.application;

public record ConnectedNodeRelationResponse(
        GraphRelationResponse relation,
        GraphNodeResponse connectedNode
) {
}
