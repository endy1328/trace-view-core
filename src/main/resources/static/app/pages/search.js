import { cardLink, el, formatDate, statusTone } from "../dom.js";
import { navigate } from "../router.js";

function toPercent(value) {
    return `${Math.round((value ?? 0) * 100)}%`;
}

function nodeTypeLabel(node) {
    return node?.type?.replaceAll("_", " ") ?? "UNKNOWN";
}

function buildResultCard(node) {
    const meta = [
        nodeTypeLabel(node),
        statusTone(node.reviewStatus),
        toPercent(node.confidence)
    ];

    const linkTarget = node.type === "API_ENDPOINT"
        ? `/endpoints/${encodeURIComponent(node.id)}`
        : `/nodes/${encodeURIComponent(node.id)}`;

    const description = [
        node.sourcePath || "No source path",
        node.sourceSymbol || "No source symbol"
    ].join(" · ");

    return cardLink(node.name || node.id, description, linkTarget, meta);
}

function buildShortcut(label, description, path) {
    return el("button", {
        className: "result-card",
        on: { click: () => navigate(path) },
        children: [
            el("div", { className: "pill-row", children: [el("span", { className: "pill", text: label })] }),
            el("h3", { text: description }),
            el("p", { text: path })
        ]
    });
}

function buildSnapshotHero(snapshot) {
    return el("section", {
        className: "panel hero",
        children: [
            el("div", {
                className: "grid two",
                children: [
                    el("div", {
                        children: [
                            el("p", { className: "eyebrow", text: "Explore the code map" }),
                            el("h2", { text: "Trace relationships from screen to service in one search." }),
                            el("p", {
                                text: snapshot
                                    ? `Loaded snapshot from ${snapshot.rootPath} on ${formatDate(snapshot.createdAt)}.`
                                    : "Load a snapshot, search across analyzed nodes, and jump directly into endpoint or graph exploration."
                            })
                        ]
                    }),
                    el("div", {
                        className: "metric-grid",
                        children: snapshot ? [
                            el("article", {
                                className: "metric-card",
                                children: [
                                    el("strong", { text: String(snapshot.graph?.nodes?.length ?? 0) }),
                                    el("span", { text: "nodes indexed" })
                                ]
                            }),
                            el("article", {
                                className: "metric-card",
                                children: [
                                    el("strong", { text: String(snapshot.graph?.relations?.length ?? 0) }),
                                    el("span", { text: "relations mapped" })
                                ]
                            }),
                            el("article", {
                                className: "metric-card",
                                children: [
                                    el("strong", { text: String(snapshot.graph?.evidences?.length ?? 0) }),
                                    el("span", { text: "evidence items" })
                                ]
                            })
                        ] : [
                            el("article", {
                                className: "metric-card",
                                children: [
                                    el("strong", { text: "Search" }),
                                    el("span", { text: "Any screen, endpoint, or service name" })
                                ]
                            }),
                            el("article", {
                                className: "metric-card",
                                children: [
                                    el("strong", { text: "Graph" }),
                                    el("span", { text: "Open the relationship map" })
                                ]
                            }),
                            el("article", {
                                className: "metric-card",
                                children: [
                                    el("strong", { text: "Review" }),
                                    el("span", { text: "Inspect Draft and Approved notes" })
                                ]
                            })
                        ]
                    })
                ]
            })
        ]
    });
}

function buildResultsSection(results, query) {
    if (!query) {
        return el("section", {
            className: "panel",
            children: [
                el("p", { className: "eyebrow", text: "Suggested starting points" }),
                el("div", {
                    className: "grid two",
                    children: results.length
                        ? results.map(buildResultCard)
                        : [el("div", {
                            className: "empty-state",
                            text: "No snapshot is loaded yet. Run analysis to populate recommended nodes."
                        })]
                })
            ]
        });
    }

    if (!results.length) {
        return el("section", {
            className: "panel",
            children: [
                el("div", {
                    className: "empty-state",
                    children: [
                        el("strong", { text: "No results found" }),
                        el("p", { text: `Nothing matched "${query}". Try a screen, endpoint, service, or class name.` })
                    ]
                })
            ]
        });
    }

    return el("section", {
        className: "panel",
        children: [
            el("p", {
                className: "eyebrow",
                text: `Results for "${query}" (${results.length})`
            }),
            el("div", {
                className: "results",
                children: results.map(buildResultCard)
            })
        ]
    });
}

export async function renderSearchPage(context) {
    context.setTitle("Search");
    context.setStatus("");

    const snapshot = context.state.snapshot;
    const endpointNode = snapshot?.graph?.nodes?.find((node) => node.type === "API_ENDPOINT");
    const endpointTarget = endpointNode
        ? `/endpoints/${encodeURIComponent(endpointNode.id)}`
        : "/search";
    const quickLinks = [
        el("button", {
            className: "ghost-button",
            text: endpointNode ? "Open Endpoint" : "Search Endpoints",
            on: {
                click: () => navigate(endpointTarget)
            }
        }),
        el("button", {
            className: "ghost-button",
            text: "Open Graph",
            on: { click: () => navigate("/graph") }
        })
    ];

    context.setActions(quickLinks);

    let currentQuery = "";
    let requestVersion = 0;
    let loading = false;

    const searchInput = el("input", {
        className: "input",
        attrs: {
            type: "search",
            placeholder: "Search screens, endpoints, services, classes...",
            value: ""
        }
    });

    const submitButton = el("button", {
        className: "button",
        attrs: { type: "submit" },
        text: "Search"
    });

    const helperText = el("p", {
        className: "microcopy",
        text: snapshot
            ? `Snapshot ready: ${snapshot.rootPath}`
            : "No snapshot loaded yet. Search will work after analysis is run."
    });

    const shortcuts = el("div", {
        className: "grid two",
        children: [
            buildShortcut(
                endpointNode ? "Endpoint Detail" : "Endpoints",
                endpointNode ? endpointNode.name || endpointNode.id : "Browse API endpoints",
                endpointTarget
            ),
            buildShortcut("Graph", "Inspect the call graph", "/graph")
        ]
    });

    const hero = buildSnapshotHero(snapshot);
    const resultsSlot = el("div", { className: "app-content" });

    async function runSearch(query) {
        const version = ++requestVersion;
        loading = true;
        submitButton.textContent = "Searching...";
        context.setStatus(query ? `Searching for "${query}"...` : "Loading suggestions...");
        resultsSlot.replaceChildren(el("section", {
            className: "panel",
            children: [el("div", { className: "empty-state", text: "Searching..." })]
        }));

        try {
            let results = [];
            if (query.trim()) {
                results = await context.api.search(query.trim());
            } else if (snapshot?.graph?.nodes) {
                results = [...snapshot.graph.nodes]
                    .sort((a, b) => (b.confidence ?? 0) - (a.confidence ?? 0))
                    .slice(0, 8);
            }
            if (version !== requestVersion) {
                return;
            }
            context.setStatus("");
            resultsSlot.replaceChildren(buildResultsSection(results, query.trim()));
        } catch (error) {
            if (version !== requestVersion) {
                return;
            }
            context.setStatus(error.message || "Search failed", "error");
            resultsSlot.replaceChildren(el("section", {
                className: "panel",
                children: [
                    el("div", {
                        className: "empty-state",
                        children: [
                            el("strong", { text: "Search failed" }),
                            el("p", { text: error.message || "Unexpected search error." })
                        ]
                    })
                ]
            }));
        } finally {
            if (version === requestVersion) {
                loading = false;
                submitButton.textContent = "Search";
            }
        }
    }

    const form = el("form", {
        className: "panel",
        children: [
            el("div", {
                className: "hero",
                children: [
                    el("div", {
                        children: [
                            el("p", { className: "eyebrow", text: "Unified search" }),
                            el("h2", { text: "Find screens, API endpoints, services, and review notes fast." }),
                            el("p", {
                                text: "Use one search bar to move from discovery into detail or graph exploration."
                            })
                        ]
                    }),
                    el("div", {
                        className: "search-form",
                        children: [
                            searchInput,
                            submitButton
                        ]
                    }),
                    helperText
                ]
            })
        ],
        on: {
            submit: (event) => {
                event.preventDefault();
                if (loading) {
                    return;
                }
                currentQuery = searchInput.value;
                runSearch(currentQuery);
            }
        }
    });

    const landing = el("section", {
        className: "panel",
        children: [
            el("div", {
                className: "pill-row",
                children: [
                    el("span", { className: "pill", text: "Search" }),
                    el("span", { className: "pill", text: "Detail" }),
                    el("span", { className: "pill", text: "Graph" }),
                    el("span", { className: "pill", text: "Review" })
                ]
            }),
            el("p", {
                className: "microcopy",
                text: "Start broad, then drill into the exact node, endpoint, or service chain."
            })
        ]
    });

    await runSearch("");

    return el("section", {
        className: "app-content",
        children: [
            hero,
            form,
            landing,
            shortcuts,
            resultsSlot
        ]
    });
}
