import { navigate } from "./router.js";

export function el(tag, options = {}) {
    const node = document.createElement(tag);
    const {
        className,
        text,
        html,
        attrs = {},
        children = [],
        on = {}
    } = options;

    if (className) {
        node.className = className;
    }
    if (text !== undefined) {
        node.textContent = text;
    }
    if (html !== undefined) {
        node.innerHTML = html;
    }
    for (const [key, value] of Object.entries(attrs)) {
        if (value !== undefined && value !== null) {
            node.setAttribute(key, value);
        }
    }
    for (const child of children) {
        if (child) {
            node.append(child);
        }
    }
    for (const [eventName, handler] of Object.entries(on)) {
        node.addEventListener(eventName, handler);
    }
    return node;
}

export function cardLink(title, description, href, meta = []) {
    return el("a", {
        className: "result-card",
        attrs: { href: `#${href}` },
        children: [
            el("div", { className: "pill-row", children: meta.map((item) => el("span", { className: "pill", text: item })) }),
            el("h3", { text: title }),
            el("p", { text: description })
        ]
    });
}

export function formatDate(value) {
    if (!value) {
        return "-";
    }
    return new Intl.DateTimeFormat("ko-KR", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
}

export function prettyName(name) {
    return name.replace(/_/g, " ").toLowerCase();
}

export function statusTone(status) {
    switch (status) {
        case "APPROVED":
            return "Approved";
        case "REJECTED":
            return "Rejected";
        case "DRAFT":
            return "Draft";
        default:
            return status || "Unknown";
    }
}

export function relationSummary(relation) {
    return `${relation.fromName} ${relation.relationType} ${relation.toName}`;
}

export function addActionButton(container, label, path) {
    container.append(el("button", {
        className: "ghost-button",
        text: label,
        on: { click: () => navigate(path) }
    }));
}
