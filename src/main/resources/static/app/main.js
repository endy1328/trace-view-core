import { api } from "./api.js";
import { el, formatDate } from "./dom.js";
import { registerRoute, matchRoute, currentPath } from "./router.js";
import { state } from "./state.js";
import { renderSearchPage } from "./pages/search.js";
import { renderDetailPage } from "./pages/detail.js";
import { renderGraphPage } from "./pages/graph.js";
import { renderReviewsPage } from "./pages/reviews.js";

const titleNode = document.getElementById("page-title");
const contentNode = document.getElementById("app-content");
const statusNode = document.getElementById("app-status");
const headerActionsNode = document.getElementById("header-actions");
const snapshotSummaryNode = document.getElementById("snapshot-summary");
const navNodes = [...document.querySelectorAll("[data-nav]")];

registerRoute("/search", renderSearchPage, { nav: "search" });
registerRoute("/nodes/:id", renderDetailPage, { nav: "search" });
registerRoute("/endpoints/:id", renderDetailPage, { nav: "search" });
registerRoute("/graph", renderGraphPage, { nav: "graph" });
registerRoute("/reviews", renderReviewsPage, { nav: "reviews" });

async function bootstrapSnapshot() {
    try {
        state.snapshot = await api.latest();
        renderSnapshotSummary();
    } catch (error) {
        renderSnapshotSummary(error);
    }
}

function renderSnapshotSummary(error) {
    snapshotSummaryNode.innerHTML = "";
    if (error || !state.snapshot) {
        snapshotSummaryNode.append(
            el("strong", { text: "No active snapshot" }),
            el("span", { text: "Run the sample analysis API to seed the UI." })
        );
        return;
    }
    snapshotSummaryNode.append(
        el("strong", { text: state.snapshot.rootPath }),
        el("span", { text: `${state.snapshot.graph.nodes.length} nodes, ${state.snapshot.graph.relations.length} relations` }),
        el("span", { text: formatDate(state.snapshot.createdAt) })
    );
}

function setTitle(title) {
    titleNode.textContent = title;
}

function setStatus(message, tone = "") {
    if (!message) {
        statusNode.hidden = true;
        statusNode.textContent = "";
        statusNode.className = "status-banner";
        return;
    }
    statusNode.hidden = false;
    statusNode.textContent = message;
    statusNode.className = `status-banner${tone ? ` is-${tone}` : ""}`;
}

function setActions(actions = []) {
    headerActionsNode.replaceChildren(...actions);
}

function updateNav(route) {
    navNodes.forEach((node) => {
        node.classList.toggle("is-active", node.dataset.nav === route?.meta?.nav);
    });
}

async function renderRoute() {
    const route = matchRoute(window.location.hash || "#/search");
    updateNav(route);

    if (!route) {
        setTitle("Not Found");
        contentNode.replaceChildren(el("section", {
            className: "empty-state",
            text: `No route matched ${currentPath()}`
        }));
        return;
    }

    setStatus("");
    setActions([]);
    contentNode.replaceChildren(el("section", {
        className: "panel",
        text: "Loading..."
    }));

    try {
        const view = await route.handler({
            api,
            state,
            params: route.params,
            setTitle,
            setStatus,
            setActions
        });
        contentNode.replaceChildren(view);
    } catch (error) {
        setStatus(error.message || "Unexpected error", "error");
        contentNode.replaceChildren(el("section", {
            className: "empty-state",
            text: "The page could not be rendered."
        }));
    }
}

window.addEventListener("hashchange", renderRoute);

await bootstrapSnapshot();
await renderRoute();
