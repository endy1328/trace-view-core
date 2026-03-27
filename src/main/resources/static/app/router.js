const routes = [];

function compile(pattern) {
    const paramNames = [];
    const regex = new RegExp(`^${pattern.replace(/:[^/]+/g, (segment) => {
        paramNames.push(segment.slice(1));
        return "([^/]+)";
    })}$`);
    return { regex, paramNames };
}

export function registerRoute(pattern, handler, meta = {}) {
    const compiled = compile(pattern);
    routes.push({ pattern, handler, meta, ...compiled });
}

export function matchRoute(hash) {
    const path = (hash.replace(/^#/, "").split("?")[0] || "/search");
    for (const route of routes) {
        const match = path.match(route.regex);
        if (!match) {
            continue;
        }
        const params = Object.fromEntries(route.paramNames.map((name, index) => [name, decodeURIComponent(match[index + 1])]));
        return { ...route, params, path };
    }
    return null;
}

export function navigate(path) {
    window.location.hash = `#${path}`;
}

export function currentPath() {
    return (window.location.hash || "#/search").replace(/^#/, "").split("?")[0];
}
