import { api } from "../api.js";
import { el, formatDate, statusTone, relationSummary } from "../dom.js";
import { navigate } from "../router.js";

function pageKindFromPath(path) {
    return path.startsWith("/endpoints/") ? "endpoint" : "node";
}

function pill(text) {
    return el("span", { className: "pill", text });
}

function keyValueRow(label, value) {
    return el("div", {
        className: "result-card",
        children: [
            el("p", { className: "eyebrow", text: label }),
            el("strong", { className: "wrap-text", text: value || "-" })
        ]
    });
}

function relationCard(relationBlock) {
    const { relation, connectedNode } = relationBlock;
    return el("article", {
        className: "relation-card",
        children: [
            el("div", {
                className: "pill-row",
                children: [
                    pill(relation.relationType),
                    pill(relation.certaintyType),
                    pill(`confidence ${relation.confidence.toFixed(2)}`)
                ]
            }),
            el("h4", { className: "wrap-text", text: connectedNode?.name || relation.toName || relation.toId }),
            el("p", { className: "wrap-text", text: relationSummary({
                fromName: relation.fromName,
                relationType: relation.relationType,
                toName: relation.toName
            }) }),
            el("p", {
                className: "muted wrap-text",
                text: `Relation ${relation.id}`
            }),
            el("div", {
                className: "meta-row",
                children: [
                    pill(`from ${relation.fromId}`),
                    pill(`to ${relation.toId}`),
                    pill(`${relation.evidenceIds.length} evidence`)
                ]
            })
        ]
    });
}

function evidenceCard(evidence) {
    return el("article", {
        className: "result-card",
        children: [
            el("div", { className: "pill-row", children: [pill(evidence.evidenceType), pill(evidence.analyzerName)] }),
            el("h4", { className: "wrap-text", text: evidence.sourceFile || evidence.sourceSymbol || evidence.id }),
            el("p", { className: "wrap-text", text: `Line ${evidence.sourceLine ?? "-"} · ${evidence.sourceSymbol || "-"}` }),
            el("p", { className: "muted wrap-text", text: `Rule ${evidence.ruleId || "-"} · ${evidence.analyzerVersion || "-"}` })
        ]
    });
}

function annotationCard(annotation) {
    return el("article", {
        className: "annotation-card",
        children: [
            el("div", {
                className: "pill-row",
                children: [
                    pill(statusTone(annotation.status)),
                    pill(annotation.author || "-"),
                    pill(formatDate(annotation.updatedAt))
                ]
            }),
            el("p", { className: "wrap-text", text: annotation.content }),
            annotation.approver ? el("p", { className: "muted wrap-text", text: `Approver: ${annotation.approver}` }) : null,
            annotation.rejectionReason ? el("p", { className: "danger-text wrap-text", text: annotation.rejectionReason }) : null
        ]
    });
}

function reviewPathForTarget(targetId) {
    return `/reviews?targetId=${encodeURIComponent(targetId)}`;
}

function renderMetadata(node) {
    const tags = Array.isArray(node.tags) ? node.tags : [...(node.tags || [])];
    const metadataEntries = Object.entries(node.metadata || {});
    return el("section", {
        className: "panel",
        children: [
            el("div", {
                className: "grid two",
                children: [
                    keyValueRow("Type", node.type),
                    keyValueRow("Confidence", node.confidence.toFixed(2)),
                    keyValueRow("Review", statusTone(node.reviewStatus)),
                    keyValueRow("Source Path", node.sourcePath || "-"),
                    keyValueRow("Source Symbol", node.sourceSymbol || "-"),
                    keyValueRow("Tags", tags.length ? tags.join(", ") : "-")
                ]
            }),
            metadataEntries.length
                ? el("div", {
                    className: "results",
                    children: metadataEntries.map(([key, value]) => keyValueRow(key, value))
                })
                : el("div", {
                    className: "empty-state",
                    text: "No extra metadata recorded for this node."
                })
        ]
    });
}

function renderRelations(title, items) {
    return el("section", {
        className: "panel",
        children: [
            el("div", {
                className: "hero",
                children: [
                    el("div", { className: "pill-row", children: [pill(title)] }),
                    el("h2", { text: `${items.length} relation${items.length === 1 ? "" : "s"}` })
                ]
            }),
            items.length
                ? el("div", { className: "relation-list", children: items.map(relationCard) })
                : el("div", { className: "empty-state", text: "No relations found." })
        ]
    });
}

function renderEvidence(evidences) {
    return el("section", {
        className: "panel",
        children: [
            el("div", {
                className: "hero",
                children: [
                    el("div", { className: "pill-row", children: [pill("Evidence")] }),
                    el("h2", { text: `${evidences.length} evidence item${evidences.length === 1 ? "" : "s"}` })
                ]
            }),
            evidences.length
                ? el("div", { className: "results", children: evidences.map(evidenceCard) })
                : el("div", { className: "empty-state", text: "No evidence is attached to this node." })
        ]
    });
}

function renderAnnotationSummary(annotations, targetId) {
    const counts = annotations.reduce((acc, annotation) => {
        acc.total += 1;
        acc[annotation.status.toLowerCase()] += 1;
        return acc;
    }, { total: 0, draft: 0, approved: 0, rejected: 0 });

    return el("section", {
        className: "panel",
        children: [
            el("div", {
                className: "hero",
                children: [
                    el("div", { className: "pill-row", children: [pill("Annotations")] }),
                    el("h2", { text: `${counts.total} annotation${counts.total === 1 ? "" : "s"}` })
                ]
            }),
            el("div", {
                className: "metric-grid",
                children: [
                    el("article", { className: "metric-card", children: [el("strong", { text: String(counts.total) }), el("span", { text: "Total" })] }),
                    el("article", { className: "metric-card", children: [el("strong", { text: String(counts.draft) }), el("span", { text: "Draft" })] }),
                    el("article", { className: "metric-card", children: [el("strong", { text: String(counts.approved) }), el("span", { text: "Approved" })] }),
                    el("article", { className: "metric-card", children: [el("strong", { text: String(counts.rejected) }), el("span", { text: "Rejected" })] })
                ]
            }),
            targetId
                ? el("div", {
                    className: "action-row",
                    children: [
                        el("button", {
                            className: "button",
                            text: "Open reviews",
                            on: { click: () => navigate(reviewPathForTarget(targetId)) }
                        })
                    ]
                })
                : null,
            annotations.length
                ? el("div", { className: "annotation-list", children: annotations.map(annotationCard) })
                : el("div", { className: "empty-state", text: "No annotations recorded yet." })
        ]
    });
}

function renderServiceChain(serviceChain) {
    const graph = serviceChain.graph;
    return el("section", {
        className: "panel",
        children: [
            el("div", {
                className: "hero",
                children: [
                    el("div", { className: "pill-row", children: [pill("Service Chain")] }),
                    el("h2", { text: `${graph.nodeCount} nodes and ${graph.relationCount} relations in the chain` })
                ]
            }),
            el("div", {
                className: "split",
                children: [
                    el("div", {
                        className: "graph-list",
                        children: [
                            el("div", {
                                className: "pill-row",
                                children: graph.nodes.map((node) => pill(`${node.type}: ${node.name}`))
                            }),
                            el("div", {
                                className: "relation-list",
                                children: graph.relations.map((relation) => relationCard({
                                    relation,
                                    connectedNode: graph.nodes.find((node) => node.id === relation.toId)
                                }))
                            })
                        ]
                    }),
                    el("div", {
                        className: "panel",
                        children: [
                            el("h3", { text: "Chain details" }),
                            el("p", { className: "muted", text: "The endpoint expands through controller, service, repository, and external nodes when present." }),
                            el("div", {
                                className: "results",
                                children: graph.nodes.map((node) => keyValueRow(node.type, `${node.name} · ${node.id}`))
                            })
                        ]
                    })
                ]
            })
        ]
    });
}

export async function renderDetailPage(context) {
    const { api, params, setTitle, setStatus, setActions } = context;
    const nodeId = params.id;
    const kind = pageKindFromPath(window.location.hash.replace(/^#/, ""));

    setTitle(kind === "endpoint" ? "Endpoint Detail" : "Node Detail");
    setStatus(`Loading ${kind} detail for ${nodeId}...`);
    setActions([
        el("button", {
            className: "ghost-button",
            text: "Open graph",
            on: { click: () => navigate(`/graph?nodeId=${encodeURIComponent(nodeId)}`) }
        }),
        el("button", {
            className: "ghost-button",
            text: "Open reviews",
            on: { click: () => navigate(reviewPathForTarget(nodeId)) }
        })
    ]);

    const [detail, serviceChain] = await Promise.all([
        api.node(nodeId),
        kind === "endpoint" ? api.serviceChain(nodeId) : Promise.resolve(null)
    ]);

    const node = detail.node;
    setTitle(`${node.name} · ${kind === "endpoint" ? "Endpoint" : "Node"}`);
    setStatus("");

    const incoming = detail.incomingRelations || [];
    const outgoing = detail.outgoingRelations || [];
    const evidences = detail.evidences || [];
    const annotations = detail.annotations || [];

    return el("div", {
        className: "grid",
        children: [
            el("section", {
                className: "panel",
                children: [
                    el("div", {
                        className: "hero",
                        children: [
                            el("div", {
                                className: "pill-row",
                                children: [
                                    pill(node.type),
                                    pill(statusTone(node.reviewStatus)),
                                    pill(node.id)
                                ]
                            }),
                            el("h2", { className: "wrap-text", text: node.name }),
                            el("p", {
                                className: "muted wrap-text",
                                text: node.sourcePath || "No source path recorded"
                            })
                        ]
                    }),
                    el("div", {
                        className: "action-row",
                        children: [
                            el("a", { className: "ghost-button", attrs: { href: `#/graph?nodeId=${encodeURIComponent(node.id)}` }, text: "Graph view" }),
                            el("a", { className: "ghost-button", attrs: { href: `#${reviewPathForTarget(node.id)}` }, text: "Review hub" })
                        ]
                    })
                ]
            }),
            renderMetadata(node),
            renderRelations("Incoming", incoming),
            renderRelations("Outgoing", outgoing),
            renderEvidence(evidences),
            renderAnnotationSummary(annotations, node.id),
            kind === "endpoint" && serviceChain ? renderServiceChain(serviceChain) : null
        ]
    });
}
