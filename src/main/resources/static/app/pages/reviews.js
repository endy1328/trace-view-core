import { api } from "../api.js";
import { el, formatDate, prettyName, statusTone } from "../dom.js";
import { navigate } from "../router.js";

function parseTargetId() {
    const hash = window.location.hash || "#/reviews";
    const queryIndex = hash.indexOf("?");
    if (queryIndex === -1) {
        return "";
    }
    return new URLSearchParams(hash.slice(queryIndex + 1)).get("targetId") || "";
}

function reviewLabel(annotation) {
    return `${prettyName(annotation.status)} review`;
}

async function resolveDetailPath(targetId) {
    if (!targetId) {
        return "/search";
    }
    const detail = await api.node(targetId);
    return detail.node?.type === "API_ENDPOINT"
        ? `/endpoints/${encodeURIComponent(targetId)}`
        : `/nodes/${encodeURIComponent(targetId)}`;
}

function annotationCard(annotation, context, refresh) {
    const statusClass = annotation.status === "APPROVED"
        ? "pill"
        : annotation.status === "REJECTED"
            ? "pill danger-text"
            : "pill warn-text";

    const approveInput = el("input", {
        className: "input",
        attrs: {
            type: "text",
            placeholder: "Approver",
            value: "lead"
        }
    });
    const rejectApproverInput = el("input", {
        className: "input",
        attrs: {
            type: "text",
            placeholder: "Approver",
            value: "lead"
        }
    });
    const rejectReasonInput = el("input", {
        className: "input",
        attrs: {
            type: "text",
            placeholder: "Rejection reason",
            value: "Needs more evidence"
        }
    });

    const detailLink = annotation.targetId
        ? el("button", {
            className: "ghost-button",
            attrs: { type: "button" },
            text: `Open ${annotation.targetId}`,
            on: {
                click: async () => {
                    context.setStatus(`Resolving ${annotation.targetId}...`);
                    try {
                        navigate(await resolveDetailPath(annotation.targetId));
                    } catch (error) {
                        context.setStatus(error.message || "Unable to open target detail", "error");
                    }
                }
            }
        })
        : null;

    const approveForm = el("form", {
        className: "inline-form",
        children: [
            approveInput,
            el("button", { className: "button", text: "Approve", attrs: { type: "submit" } })
        ],
        on: {
            submit: async (event) => {
                event.preventDefault();
                context.setStatus(`Approving ${annotation.id}...`);
                try {
                    await api.approveAnnotation(annotation.id, approveInput.value.trim());
                    context.setStatus("Annotation approved.");
                    await refresh();
                } catch (error) {
                    context.setStatus(error.message || "Approve failed", "error");
                }
            }
        }
    });

    const rejectForm = el("form", {
        className: "inline-form",
        children: [
            rejectApproverInput,
            rejectReasonInput,
            el("button", { className: "ghost-button", text: "Reject", attrs: { type: "submit" } })
        ],
        on: {
            submit: async (event) => {
                event.preventDefault();
                context.setStatus(`Rejecting ${annotation.id}...`);
                try {
                    await api.rejectAnnotation(
                        annotation.id,
                        rejectApproverInput.value.trim(),
                        rejectReasonInput.value.trim()
                    );
                    context.setStatus("Annotation rejected.");
                    await refresh();
                } catch (error) {
                    context.setStatus(error.message || "Reject failed", "error");
                }
            }
        }
    });

    return el("article", {
        className: "annotation-card",
        children: [
            el("div", {
                className: "pill-row",
                children: [
                    el("span", { className: statusClass, text: statusTone(annotation.status) }),
                    el("span", { className: "pill", text: annotation.targetId || "Unassigned target" }),
                    el("span", { className: "pill", text: reviewLabel(annotation) }),
                    detailLink
                ].filter(Boolean)
            }),
            el("h3", { className: "wrap-text", text: annotation.content }),
            el("p", { className: "microcopy wrap-text", text: `Author ${annotation.author}` }),
            el("div", {
                className: "meta-row",
                children: [
                    el("span", { className: "pill", text: `Created ${formatDate(annotation.createdAt)}` }),
                    el("span", { className: "pill", text: `Updated ${formatDate(annotation.updatedAt)}` }),
                    el("span", { className: "pill", text: `Approver ${annotation.approver || "-"}` }),
                    el("span", { className: "pill", text: `Reason ${annotation.rejectionReason || "-"}` })
                ]
            }),
            el("div", { className: "action-row", children: [approveForm, rejectForm] })
        ]
    });
}

export async function renderReviewsPage(context) {
    const targetId = parseTargetId();
    const isTargeted = Boolean(targetId);
    context.setTitle(isTargeted ? `Reviews / ${targetId}` : "Reviews");

    let annotations = [];
    let loadError = "";
    let rootNode = null;

    const title = isTargeted ? `Review queue for ${targetId}` : "Pending review queue";
    const subtitle = isTargeted
        ? "Showing annotations for the selected node. Create a new review, approve, or reject in place."
        : "Default view shows pending annotations only. Use target lookup to inspect a specific node.";

    const targetLookupInput = el("input", {
        className: "input",
        attrs: {
            type: "text",
            placeholder: "Target node id, e.g. service_order",
            value: targetId
        }
    });

    const authorInput = el("input", {
        className: "input",
        attrs: { type: "text", placeholder: "Author", value: "reviewer" }
    });
    const contentInput = el("textarea", {
        className: "textarea",
        attrs: { placeholder: "Annotation content" }
    });
    const targetInput = el("input", {
        className: "input",
        attrs: {
            type: "text",
            placeholder: "Target node id",
            value: targetId,
            readonly: isTargeted ? "readonly" : null
        }
    });

    function renderView() {
        const next = buildPage();
        if (rootNode && rootNode.isConnected) {
            rootNode.replaceWith(next);
        }
        rootNode = next;
        return next;
    }

    const refresh = async () => {
        context.setStatus("Loading reviews...");
        try {
            annotations = await api.listAnnotations(targetId);
            loadError = "";
            renderView();
            context.setStatus("");
        } catch (error) {
            annotations = [];
            loadError = error.message || "Failed to load annotations";
            renderView();
            context.setStatus(loadError, "error");
        }
    };

    function openTarget(event) {
        event.preventDefault();
        const nextTargetId = targetLookupInput.value.trim();
        if (!nextTargetId) {
            navigate("/reviews");
            return;
        }
        navigate(`/reviews?targetId=${encodeURIComponent(nextTargetId)}`);
    }

    function buildPage() {
        const createForm = el("form", {
            className: "grid",
            children: [
                el("div", {
                    className: "grid two",
                    children: [
                        targetInput,
                        authorInput
                    ]
                }),
                contentInput,
                el("button", { className: "button", text: "Create annotation", attrs: { type: "submit" } })
            ],
            on: {
                submit: async (event) => {
                    event.preventDefault();
                    context.setStatus("Creating annotation...");
                    try {
                        await api.createAnnotation({
                            targetId: targetInput.value.trim(),
                            content: contentInput.value.trim(),
                            author: authorInput.value.trim()
                        });
                        contentInput.value = "";
                        context.setStatus("Annotation created.");
                        await refresh();
                    } catch (error) {
                        context.setStatus(error.message || "Create failed", "error");
                    }
                }
            }
        });

        const lookupForm = el("form", {
            className: "inline-form",
            children: [
                targetLookupInput,
                el("button", { className: "ghost-button", text: "Open target", attrs: { type: "submit" } }),
                el("button", {
                    className: "ghost-button",
                    text: "Show pending",
                    attrs: { type: "button" },
                    on: { click: () => navigate("/reviews") }
                })
            ],
            on: { submit: openTarget }
        });

        const actionHeader = el("div", {
            className: "action-row",
            children: [
                el("a", { className: "pill", attrs: { href: "#/search" }, text: "Back to search" }),
                el("a", { className: "pill", attrs: { href: "#/graph" }, text: "Open graph" })
            ]
        });

        if (isTargeted) {
            actionHeader.append(
                el("button", {
                    className: "ghost-button",
                    attrs: { type: "button" },
                    text: "Open target detail",
                    on: {
                        click: async () => {
                            context.setStatus(`Resolving ${targetId}...`);
                            try {
                                navigate(await resolveDetailPath(targetId));
                            } catch (error) {
                                context.setStatus(error.message || "Unable to open target detail", "error");
                            }
                        }
                    }
                })
            );
        }

        return el("section", {
            className: "grid",
            children: [
                el("section", {
                    className: "panel hero",
                    children: [
                        el("p", { className: "eyebrow", text: "Review UI" }),
                        el("h2", { text: title }),
                        el("p", { text: subtitle }),
                        actionHeader
                    ]
                }),
                el("section", {
                    className: "panel grid",
                    children: [
                        el("div", {
                            className: "metric-grid",
                            children: [
                                el("div", { className: "metric-card", children: [el("strong", { text: String(annotations.length) }), el("span", { text: isTargeted ? "annotations for target" : "pending annotations" })] }),
                                el("div", { className: "metric-card", children: [el("strong", { text: isTargeted ? "targeted" : "pending" }), el("span", { text: isTargeted ? `Filtered by ${targetId}` : "Loaded from pending queue" })] }),
                                el("div", { className: "metric-card", children: [el("strong", { text: "live" }), el("span", { text: "Create / approve / reject" })] })
                            ]
                        }),
                        lookupForm,
                        createForm
                    ]
                }),
                el("section", {
                    className: "panel",
                    children: [
                        el("h3", { text: loadError ? "Unable to load annotations" : isTargeted ? "Target annotations" : "Pending annotations" }),
                        loadError
                            ? el("div", { className: "empty-state danger-text", text: loadError })
                            : annotations.length
                                ? el("div", { className: "annotation-list", children: annotations.map((annotation) => annotationCard(annotation, context, refresh)) })
                                : el("div", { className: "empty-state", text: isTargeted ? "No annotations found for this target." : "No pending annotations right now." })
                    ]
                })
            ]
        });
    }

    context.setActions([
        el("a", { className: "ghost-button", attrs: { href: "#/search" }, text: "Search" }),
        el("a", { className: "ghost-button", attrs: { href: "#/graph" }, text: "Graph" })
    ]);

    await refresh();
    return rootNode;
}
