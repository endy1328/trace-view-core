const JSON_HEADERS = {
    "Content-Type": "application/json"
};

async function request(path, options = {}) {
    const response = await fetch(path, {
        headers: JSON_HEADERS,
        ...options
    });

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || `Request failed: ${response.status}`);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

export const api = {
    latest: () => request("/api/query/latest"),
    search: (query) => request(`/api/query/search?q=${encodeURIComponent(query)}`),
    endpoints: () => request("/api/query/endpoints"),
    node: (id) => request(`/api/query/nodes/${encodeURIComponent(id)}`),
    serviceChain: (id) => request(`/api/query/endpoints/${encodeURIComponent(id)}/service-chain`),
    graph: ({ nodeId = "", type = "" } = {}) => {
        const params = new URLSearchParams();
        if (nodeId) {
            params.set("nodeId", nodeId);
        }
        if (type) {
            params.set("type", type);
        }
        const suffix = params.toString() ? `?${params.toString()}` : "";
        return request(`/api/query/graph${suffix}`);
    },
    listAnnotations: (targetId = "") => {
        const suffix = targetId ? `?targetId=${encodeURIComponent(targetId)}` : "";
        return request(`/api/reviews/annotations${suffix}`);
    },
    createAnnotation: (payload) => request("/api/reviews/annotations", {
        method: "POST",
        body: JSON.stringify(payload)
    }),
    approveAnnotation: (id, approver) => request(`/api/reviews/annotations/${encodeURIComponent(id)}/approve`, {
        method: "POST",
        body: JSON.stringify({ approver })
    }),
    rejectAnnotation: (id, approver, reason) => request(`/api/reviews/annotations/${encodeURIComponent(id)}/reject`, {
        method: "POST",
        body: JSON.stringify({ approver, reason })
    }),
    runAnalysis: (rootPath) => request("/api/analysis/run", {
        method: "POST",
        body: JSON.stringify({ rootPath })
    })
};
