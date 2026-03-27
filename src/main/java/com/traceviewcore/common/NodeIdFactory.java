package com.traceviewcore.common;

public final class NodeIdFactory {

    private NodeIdFactory() {
    }

    public static String endpoint(String httpMethod, String path) {
        return stable("endpoint", httpMethod + ":" + path);
    }

    public static String entryPoint(String className, String methodName) {
        return stable("entrypoint", className + "#" + methodName);
    }

    public static String service(String className, String methodName) {
        return stable("service", className + "#" + methodName);
    }

    public static String repository(String typeName) {
        return stable("repository", typeName);
    }

    public static String external(String typeName) {
        return stable("external", typeName);
    }

    private static String stable(String prefix, String value) {
        return prefix + "_" + value.replaceAll("[^A-Za-z0-9]+", "_").toLowerCase();
    }
}
