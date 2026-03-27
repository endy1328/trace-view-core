import { api } from "../api.js";
import {
    el,
    formatDate,
    prettyName,
    relationSummary,
    statusTone
} from "../dom.js";

const NODE_WIDTH = 190;
const NODE_HEIGHT = 124;

function readFilters() {
    const searchParams = new URLSearchParams(window.location.search);
    const hashParams = new URLSearchParams((window.location.hash.split("?")[1] || ""));
    return {
        nodeId: searchParams.get("nodeId") || hashParams.get("nodeId") || "",
        type: searchParams.get("type") || hashParams.get("type") || ""
    };
}

function syncUrl(filters) {
    const search = new URLSearchParams();
    if (filters.nodeId) {
        search.set("nodeId", filters.nodeId);
    }
    if (filters.type) {
        search.set("type", filters.type);
    }
    const query = search.toString();
    const hash = window.location.hash || "#/graph";
    const nextUrl = `${window.location.pathname}${query ? `?${query}` : ""}${hash}`;
    window.history.replaceState(null, "", nextUrl);
}

function uniqueTypes(nodes) {
    return [...new Set(nodes.map((node) => node.type).filter(Boolean))].sort();
}

function buildLayout(nodes, focusNodeId, width, height) {
    const positions = new Map();
    if (nodes.length === 0) {
        return positions;
    }

    const centerX = width / 2;
    const centerY = Math.max(220, height / 2);
    const focusNode = focusNodeId ? nodes.find((node) => node.id === focusNodeId) : null;
    const orderedNodes = focusNode ? [focusNode, ...nodes.filter((node) => node.id !== focusNodeId)] : [...nodes];

    if (orderedNodes.length === 1) {
        positions.set(orderedNodes[0].id, {
            left: centerX - NODE_WIDTH / 2,
            top: centerY - NODE_HEIGHT / 2
        });
        return positions;
    }

    if (focusNode) {
        positions.set(focusNode.id, {
            left: centerX - NODE_WIDTH / 2,
            top: centerY - NODE_HEIGHT / 2
        });
    }

    const outerNodes = focusNode ? orderedNodes.slice(1) : orderedNodes;
    const ringCount = outerNodes.length > 8 ? 2 : 1;
    const perRing = Math.ceil(outerNodes.length / ringCount);
    const baseRadius = Math.min(Math.max(140, Math.min(width, height) * 0.25), 260);

    outerNodes.forEach((node, index) => {
        const ring = Math.floor(index / perRing);
        const ringNodes = outerNodes.slice(ring * perRing, (ring + 1) * perRing);
        const ringIndex = index - ring * perRing;
        const radius = baseRadius + ring * 120;
        const angleOffset = ring * (Math.PI / Math.max(3, ringNodes.length));
        const angle = (2 * Math.PI * ringIndex) / Math.max(1, ringNodes.length) - Math.PI / 2 + angleOffset;
        const left = centerX + Math.cos(angle) * radius - NODE_WIDTH / 2;
        const top = centerY + Math.sin(angle) * radius - NODE_HEIGHT / 2;
        positions.set(node.id, { left, top });
    });

    return positions;
}

function createMetric(label, value, note) {
    return el("article", {
        className: "metric-card",
        children: [
            el("span", { className: "microcopy", text: label }),
            el("strong", { text: value }),
            el("span", { text: note })
        ]
    });
}

function createRelationCard(relation, onPick, selected = false) {
    const label = `${relation.fromName} → ${relation.toName}`;
    return el("button", {
        className: `relation-card ghost-button${selected ? " is-selected" : ""}`,
        attrs: {
            type: "button",
            style: selected ? "border-color: rgba(12, 122, 106, 0.55); background: rgba(240, 251, 248, 0.96);" : ""
        },
        on: { click: () => onPick(relation.id) },
        children: [
            el("div", {
                className: "pill-row",
                children: [
                    el("span", { className: "pill", text: relation.relationType }),
                    el("span", { className: "pill", text: relation.certaintyType }),
                    el("span", { className: "pill", text: `${relation.evidenceIds.length} evidence` })
                ]
            }),
            el("strong", { className: "wrap-text", text: label }),
            el("p", { className: "microcopy", text: `Confidence ${Math.round(relation.confidence * 100)}%` })
        ]
    });
}

function createNodeCard(node, position, selected, onPick) {
    return el("button", {
        className: `graph-node-card${selected ? " is-selected" : ""}`,
        attrs: {
            type: "button",
            style: `left:${position.left}px; top:${position.top}px;`
        },
        on: { click: () => onPick(node.id) },
        children: [
            el("div", {
                className: "pill-row",
                children: [
                    el("span", { className: "pill", text: prettyName(node.type) }),
                    el("span", { className: "pill", text: statusTone(node.reviewStatus) })
                ]
            }),
            el("strong", { className: "truncate-2", text: node.name }),
            el("p", { className: "microcopy truncate-2", text: node.sourceSymbol || node.sourcePath || "No source symbol" }),
            el("p", { className: "microcopy", text: `Confidence ${Math.round(node.confidence * 100)}%` })
        ]
    });
}

function graphContextText(snapshot) {
    if (!snapshot) {
        return "No snapshot loaded";
    }
    return `${snapshot.snapshotId} • ${formatDate(snapshot.createdAt)}`;
}

function renderDetailsPanel({
    container,
    snapshot,
    selectedNode,
    selectedRelation,
    relationsByNode,
    onSelectRelation
}) {
    container.replaceChildren();

    if (!snapshot) {
        container.append(el("div", {
            className: "empty-state",
            text: "Load a snapshot to inspect the graph."
        }));
        return;
    }

    if (selectedRelation) {
        container.append(
            el("div", {
                className: "pill-row",
                children: [
                    el("span", { className: "pill", text: selectedRelation.relationType }),
                    el("span", { className: "pill", text: selectedRelation.certaintyType }),
                    el("span", { className: "pill", text: `${selectedRelation.evidenceIds.length} evidence` })
                ]
            }),
            el("h3", { className: "wrap-text", text: relationSummary(selectedRelation) }),
            el("p", {
                className: "wrap-text",
                text: `Confidence ${Math.round(selectedRelation.confidence * 100)}%.`
            }),
            el("p", {
                className: "microcopy wrap-text",
                text: `From ${selectedRelation.fromId} to ${selectedRelation.toId}`
            }),
            el("div", {
                className: "action-row",
                children: [
                    el("a", {
                        className: "ghost-button",
                        attrs: { href: `#/nodes/${encodeURIComponent(selectedRelation.fromId)}` },
                        text: "Open source node"
                    }),
                    el("a", {
                        className: "ghost-button",
                        attrs: { href: `#/nodes/${encodeURIComponent(selectedRelation.toId)}` },
                        text: "Open target node"
                    })
                ]
            })
        );
        return;
    }

    if (!selectedNode) {
        container.append(el("div", {
            className: "empty-state",
            text: "Select a node or relation to inspect its context."
        }));
        return;
    }

    const nodeRelations = relationsByNode.get(selectedNode.id) || { incoming: [], outgoing: [] };

    container.append(
        el("div", {
            className: "pill-row",
            children: [
                el("span", { className: "pill", text: prettyName(selectedNode.type) }),
                el("span", { className: "pill", text: statusTone(selectedNode.reviewStatus) }),
                el("span", { className: "pill", text: `${Math.round(selectedNode.confidence * 100)}% confidence` })
            ]
        }),
        el("h3", { className: "wrap-text", text: selectedNode.name }),
        el("p", { className: "microcopy wrap-text", text: selectedNode.sourceSymbol || selectedNode.sourcePath || "No source symbol" }),
        el("p", { className: "wrap-text", text: `Snapshot ${graphContextText(snapshot)}` }),
        el("div", {
            className: "grid two",
            children: [
                el("div", {
                    children: [
                        el("p", { className: "eyebrow", text: "Source" }),
                        el("p", { className: "wrap-text", text: selectedNode.sourcePath || "-" }),
                        el("p", { className: "microcopy wrap-text", text: selectedNode.sourceSymbol || "-" })
                    ]
                }),
                el("div", {
                    children: [
                        el("p", { className: "eyebrow", text: "Tags" }),
                        selectedNode.tags.size > 0
                            ? el("div", {
                                className: "pill-row",
                                children: [...selectedNode.tags].map((tag) => el("span", { className: "pill", text: tag }))
                            })
                            : el("p", { className: "microcopy", text: "No tags attached." })
                    ]
                })
            ]
        }),
        el("div", {
            className: "panel",
            children: [
                el("p", { className: "eyebrow", text: "Metadata" }),
                selectedNode.metadata && Object.keys(selectedNode.metadata).length > 0
                    ? el("div", {
                        className: "results",
                        children: Object.entries(selectedNode.metadata).map(([key, value]) => el("div", {
                            className: "result-card",
                            children: [
                                el("strong", { className: "wrap-text", text: key }),
                                el("p", { className: "microcopy wrap-text", text: String(value) })
                            ]
                        }))
                    })
                    : el("p", { className: "microcopy", text: "No metadata available." })
            ]
        }),
        el("div", {
            className: "panel",
            children: [
                el("p", { className: "eyebrow", text: "Node Relations" }),
                el("p", { className: "microcopy", text: `${nodeRelations.incoming.length} incoming, ${nodeRelations.outgoing.length} outgoing` }),
                nodeRelations.incoming.length > 0
                    ? el("div", {
                        className: "results",
                        children: [
                            el("div", {
                                children: [
                                    el("p", { className: "eyebrow", text: "Incoming" }),
                                    el("div", {
                                        className: "relation-list",
                                        children: nodeRelations.incoming.map((relation) => createRelationCard(relation, onSelectRelation, false))
                                    })
                                ]
                            }),
                            el("div", {
                                children: [
                                    el("p", { className: "eyebrow", text: "Outgoing" }),
                                    nodeRelations.outgoing.length > 0
                                        ? el("div", {
                                            className: "relation-list",
                                            children: nodeRelations.outgoing.map((relation) => createRelationCard(relation, onSelectRelation, false))
                                        })
                                        : el("p", { className: "microcopy", text: "No outgoing relations." })
                                ]
                            })
                        ]
                    })
                    : el("p", { className: "microcopy", text: "No incoming relations." })
            ]
        }),
        el("div", {
            className: "action-row",
            children: [
                el("a", {
                    className: "button",
                    attrs: { href: `#/nodes/${encodeURIComponent(selectedNode.id)}` },
                    text: "Open detail"
                }),
                el("a", {
                    className: "ghost-button",
                    attrs: { href: `#/reviews?targetId=${encodeURIComponent(selectedNode.id)}` },
                    text: "Review annotations"
                })
            ]
        })
    );
}

function renderGraphLines(svg, nodes, relations, positions) {
    svg.replaceChildren();

    const defs = el("defs", {
        children: [
            el("marker", {
                attrs: {
                    id: "arrowhead",
                    markerWidth: "10",
                    markerHeight: "7",
                    refX: "9",
                    refY: "3.5",
                    orient: "auto",
                    markerUnits: "strokeWidth"
                },
                children: [
                    el("polygon", {
                        attrs: {
                            points: "0 0, 10 3.5, 0 7",
                            fill: "rgba(12, 122, 106, 0.55)"
                        }
                    })
                ]
            })
        ]
    });
    svg.append(defs);

    relations.forEach((relation) => {
        const from = positions.get(relation.fromId);
        const to = positions.get(relation.toId);
        if (!from || !to) {
            return;
        }
        const x1 = from.left + NODE_WIDTH / 2;
        const y1 = from.top + NODE_HEIGHT / 2;
        const x2 = to.left + NODE_WIDTH / 2;
        const y2 = to.top + NODE_HEIGHT / 2;
        svg.append(el("line", {
            attrs: {
                x1: String(x1),
                y1: String(y1),
                x2: String(x2),
                y2: String(y2),
                stroke: relation.certaintyType === "CERTAIN" ? "rgba(12, 122, 106, 0.7)" : "rgba(112, 96, 77, 0.42)",
                "stroke-width": relation.certaintyType === "CERTAIN" ? "2.25" : "1.5",
                "marker-end": "url(#arrowhead)"
            }
        }));
    });
}

function relationIndex(relations) {
    const byNode = new Map();
    for (const relation of relations) {
        if (!byNode.has(relation.fromId)) {
            byNode.set(relation.fromId, { incoming: [], outgoing: [] });
        }
        if (!byNode.has(relation.toId)) {
            byNode.set(relation.toId, { incoming: [], outgoing: [] });
        }
        byNode.get(relation.fromId).outgoing.push(relation);
        byNode.get(relation.toId).incoming.push(relation);
    }
    return byNode;
}

export async function renderGraphPage(context) {
    context.setTitle("Graph");

    const filters = readFilters();
    const state = {
        graph: null,
        selectedNodeId: filters.nodeId || "",
        selectedRelationId: "",
        filters: { ...filters },
        loading: true,
        error: null
    };

    const typeSelect = el("select", { className: "select" });
    const nodeInput = el("input", {
        className: "input",
        attrs: {
            type: "text",
            placeholder: "Focus node id",
            value: filters.nodeId || ""
        }
    });
    const statusNode = el("div", { className: "microcopy" });
    const summaryNode = el("div", { className: "metric-grid" });
    const canvasNode = el("div", { className: "graph-canvas" });
    const detailsNode = el("aside", { className: "panel" });
    const relationNode = el("div", { className: "relation-list" });
    const root = el("section", {
        className: "grid",
        children: [
            el("section", {
                className: "panel hero",
                children: [
                    el("div", {
                        className: "pill-row",
                        children: [
                            el("span", { className: "pill", text: "Graph explorer" }),
                            el("span", { className: "pill", text: "Static analysis" }),
                            el("span", { className: "pill", text: "Selection-aware" })
                        ]
                    }),
                    el("div", {
                        className: "grid two",
                        children: [
                            el("div", {
                                children: [
                                    el("h2", { text: "Trace relationships" }),
                                    el("p", {
                                        text: "Inspect screen, endpoint, service, repository, and external call paths as a connected graph."
                                    })
                                ]
                            }),
                            el("div", {
                                className: "panel",
                                children: [
                                    el("form", {
                                        className: "inline-form",
                                        on: {
                                            submit: async (event) => {
                                                event.preventDefault();
                                                await loadGraph({
                                                    nodeId: nodeInput.value.trim(),
                                                    type: typeSelect.value
                                                });
                                            }
                                        },
                                        children: [
                                            el("div", { children: [nodeInput] }),
                                            el("div", { children: [typeSelect] }),
                                            el("button", { className: "button", attrs: { type: "submit" }, text: "Apply" }),
                                            el("button", {
                                                className: "ghost-button",
                                                attrs: { type: "button" },
                                                text: "Reset",
                                                on: {
                                                    click: async () => {
                                                        nodeInput.value = "";
                                                        typeSelect.value = "";
                                                        await loadGraph({ nodeId: "", type: "" });
                                                    }
                                                }
                                            })
                                        ]
                                    }),
                                    statusNode
                                ]
                            })
                        ]
                    })
                ]
            }),
            summaryNode,
            el("div", {
                className: "split",
                children: [
                    el("div", {
                        className: "panel",
                        children: [
                            el("p", { className: "eyebrow", text: "Graph canvas" }),
                            canvasNode
                        ]
                    }),
                    detailsNode
                ]
            }),
            el("section", {
                className: "panel",
                children: [
                    el("p", { className: "eyebrow", text: "Relations" }),
                    relationNode
                ]
            })
        ]
    });

    const svgNode = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svgNode.setAttribute("class", "graph-lines");
    svgNode.setAttribute("width", "100%");
    svgNode.setAttribute("height", "100%");
    svgNode.setAttribute("viewBox", "0 0 1000 700");
    svgNode.setAttribute("preserveAspectRatio", "none");
    svgNode.style.position = "absolute";
    svgNode.style.inset = "0";
    canvasNode.style.position = "relative";
    canvasNode.style.minHeight = "480px";
    canvasNode.append(svgNode);

    function setLoading() {
        state.loading = true;
        state.error = null;
        statusNode.replaceChildren(el("span", { className: "microcopy", text: "Loading graph data..." }));
        canvasNode.replaceChildren(el("div", {
            className: "empty-state",
            text: "Loading graph..."
        }));
        canvasNode.append(svgNode);
        detailsNode.replaceChildren(el("div", {
            className: "empty-state",
            text: "Waiting for graph data."
        }));
        relationNode.replaceChildren(el("div", { className: "empty-state", text: "No relations yet." }));
        summaryNode.replaceChildren(
            createMetric("Nodes", "0", "Waiting for data"),
            createMetric("Relations", "0", "Waiting for data"),
            createMetric("Evidence", "0", "Waiting for data")
        );
    }

    function renderEmpty(message) {
        canvasNode.replaceChildren(el("div", {
            className: "empty-state",
            text: message
        }));
        canvasNode.append(svgNode);
        detailsNode.replaceChildren(el("div", {
            className: "empty-state",
            text: "No graph details available."
        }));
        relationNode.replaceChildren(el("div", {
            className: "empty-state",
            text: "No relations to display."
        }));
    }

    function renderGraphView() {
        if (!state.graph) {
            return;
        }

        const nodes = state.graph.nodes || [];
        const relations = state.graph.relations || [];
        const evidences = state.graph.evidences || [];
        const types = uniqueTypes(nodes);

        typeSelect.replaceChildren(
            el("option", { attrs: { value: "" }, text: "All node types" }),
            ...types.map((type) => el("option", { attrs: { value: type }, text: prettyName(type) }))
        );
        typeSelect.value = state.filters.type || "";
        nodeInput.value = state.filters.nodeId || "";

        summaryNode.replaceChildren(
            createMetric("Nodes", String(nodes.length), `Snapshot ${state.graph.snapshotId}`),
            createMetric("Relations", String(relations.length), state.graph.rootPath),
            createMetric("Evidence", String(evidences.length), graphContextText(state.graph))
        );

        const focusNode = state.selectedNodeId ? nodes.find((node) => node.id === state.selectedNodeId) : null;
        if (state.selectedNodeId && !focusNode) {
            state.selectedNodeId = "";
        }

        const width = Math.max(860, canvasNode.clientWidth || 860);
        const height = Math.max(480, 260 + Math.ceil(nodes.length / 4) * 110);
        canvasNode.style.minHeight = `${height}px`;
        canvasNode.replaceChildren(svgNode);

        if (nodes.length === 0) {
            renderEmpty("No graph nodes matched the current filters.");
            return;
        }

        const positions = buildLayout(nodes, state.selectedNodeId || "", width, height);
        svgNode.setAttribute("viewBox", `0 0 ${width} ${height}`);
        renderGraphLines(svgNode, nodes, relations, positions);

        const relationMap = new Map(relations.map((relation) => [relation.id, relation]));
        const relationsByNode = relationIndex(relations);
        const selectedNode = focusNode || nodes[0];
        if (!state.selectedNodeId) {
            state.selectedNodeId = selectedNode.id;
        }
        const selectedRelation = state.selectedRelationId ? relationMap.get(state.selectedRelationId) : null;

        const nodeCards = nodes.map((node) => {
            const position = positions.get(node.id) || { left: 0, top: 0 };
            return createNodeCard(node, position, node.id === state.selectedNodeId, (id) => {
                state.selectedNodeId = id;
                state.selectedRelationId = "";
                syncUrl({ nodeId: id, type: state.filters.type });
                renderGraphView();
            });
        });
        canvasNode.append(...nodeCards);

        renderDetailsPanel({
            container: detailsNode,
            snapshot: state.graph,
            selectedNode: selectedRelation ? null : selectedNode,
            selectedRelation,
            relationsByNode,
            onSelectRelation: (relationId) => {
                state.selectedRelationId = relationId;
                renderGraphView();
            }
        });

        relationNode.replaceChildren(
            ...relations.map((relation) => createRelationCard(relation, (id) => {
                state.selectedRelationId = id;
                renderGraphView();
            }, relation.id === state.selectedRelationId))
        );
    }

    async function loadGraph(nextFilters) {
        state.filters = {
            nodeId: nextFilters.nodeId || "",
            type: nextFilters.type || ""
        };
        state.selectedNodeId = state.filters.nodeId;
        state.selectedRelationId = "";
        syncUrl(state.filters);
        setLoading();
        try {
            const graph = await api.graph(state.filters);
            if (!graph) {
                state.graph = null;
                state.loading = false;
                state.error = null;
                statusNode.replaceChildren(
                    el("span", { className: "microcopy", text: "No snapshot loaded yet. Run analysis to populate the graph." })
                );
                renderEmpty("No snapshot loaded yet. Run analysis to inspect graph relationships.");
                return;
            }
            state.graph = graph;
            state.loading = false;
            state.error = null;
            statusNode.replaceChildren(
                el("span", { className: "microcopy", text: `Loaded ${graph.nodes.length} nodes and ${graph.relations.length} relations.` })
            );
            renderGraphView();
        } catch (error) {
            state.graph = null;
            state.loading = false;
            state.error = error;
            statusNode.replaceChildren(
                el("span", {
                    className: "danger-text",
                    text: error.message || "Failed to load graph data."
                })
            );
            renderEmpty(error.message || "Failed to load graph data.");
        }
    }

    context.setActions([
        el("a", { className: "ghost-button", attrs: { href: "#/search" }, text: "Search" }),
        el("a", { className: "ghost-button", attrs: { href: "#/reviews" }, text: "Reviews" })
    ]);

    setLoading();
    window.requestAnimationFrame(() => {
        loadGraph(filters);
    });
    return root;
}
