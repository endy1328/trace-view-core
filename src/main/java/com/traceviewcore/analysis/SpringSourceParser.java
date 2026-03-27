package com.traceviewcore.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SpringSourceParser {

    private static final Pattern CLASS_PATTERN = Pattern.compile("\\bclass\\s+(\\w+)");
    private static final Pattern INTERFACE_PATTERN = Pattern.compile("\\binterface\\s+(\\w+)");
    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\b(?:class|interface)\\s+\\w+\\s+implements\\s+([^\\{]+)");
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\b(?:class|interface)\\s+\\w+\\s+extends\\s+([^\\{]+)");
    private static final Pattern FIELD_PATTERN = Pattern.compile("(?:private|protected|public)\\s+(?:final\\s+)?([A-Z][A-Za-z0-9_<>?, ]+)\\s+(\\w+)\\s*;");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(public|protected|private)\\s+[\\w<>\\[\\], ?]+\\s+(\\w+)\\s*\\([^;]*\\)\\s*\\{");

    public ParsedClass parse(SourceDocument document) {
        String content = document.content();
        String className = extract(CLASS_PATTERN, content);
        String interfaceName = extract(INTERFACE_PATTERN, content);
        String typeName = className != null ? className : interfaceName;
        Set<String> annotations = extractAnnotations(content);
        Set<String> supertypes = extractSupertypes(content);
        List<ParsedField> fields = extractFields(content);
        List<ParsedMethod> methods = extractMethods(content);
        return new ParsedClass(document.path(), typeName, annotations, supertypes, fields, methods, content);
    }

    private String extract(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Set<String> extractAnnotations(String content) {
        Set<String> annotations = new HashSet<>();
        Matcher matcher = Pattern.compile("@([A-Za-z0-9_]+)").matcher(content);
        while (matcher.find()) {
            annotations.add(matcher.group(1));
        }
        return annotations;
    }

    private List<ParsedField> extractFields(String content) {
        List<ParsedField> fields = new ArrayList<>();
        Matcher matcher = FIELD_PATTERN.matcher(content);
        while (matcher.find()) {
            fields.add(new ParsedField(matcher.group(1).trim(), matcher.group(2).trim()));
        }
        return fields;
    }

    private Set<String> extractSupertypes(String content) {
        Set<String> supertypes = new HashSet<>();
        addTypeNames(supertypes, extract(IMPLEMENTS_PATTERN, content));
        addTypeNames(supertypes, extract(EXTENDS_PATTERN, content));
        return supertypes;
    }

    private void addTypeNames(Set<String> supertypes, String rawTypes) {
        if (rawTypes == null || rawTypes.isBlank()) {
            return;
        }
        for (String rawType : rawTypes.split(",")) {
            String normalized = rawType.replaceAll("<.*?>", "").trim();
            if (!normalized.isBlank()) {
                supertypes.add(normalized);
            }
        }
    }

    private List<ParsedMethod> extractMethods(String content) {
        List<ParsedMethod> methods = new ArrayList<>();
        String[] lines = content.split("\\R");
        String pendingAnnotation = null;
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index].trim();
            if (line.startsWith("@")) {
                pendingAnnotation = line;
                continue;
            }
            Matcher matcher = METHOD_PATTERN.matcher(line);
            if (matcher.find()) {
                String methodName = matcher.group(2).trim();
                String body = extractMethodBody(lines, index);
                methods.add(new ParsedMethod(methodName, pendingAnnotation, body, index + 1));
                pendingAnnotation = null;
            }
        }
        return methods;
    }

    private String extractMethodBody(String[] lines, int startIndex) {
        StringBuilder builder = new StringBuilder();
        int braceBalance = 0;
        boolean opened = false;
        for (int index = startIndex; index < lines.length; index++) {
            String line = lines[index];
            builder.append(line).append('\n');
            for (char character : line.toCharArray()) {
                if (character == '{') {
                    braceBalance++;
                    opened = true;
                } else if (character == '}') {
                    braceBalance--;
                }
            }
            if (opened && braceBalance == 0) {
                break;
            }
        }
        return builder.toString();
    }

    public record ParsedClass(
            String path,
            String typeName,
            Set<String> annotations,
            Set<String> supertypes,
            List<ParsedField> fields,
            List<ParsedMethod> methods,
            String content
    ) {
        public boolean hasAnnotation(String annotation) {
            return annotations.contains(annotation);
        }
    }

    public record ParsedField(String typeName, String fieldName) {
    }

    public record ParsedMethod(
            String methodName,
            String annotationLine,
            String body,
            int sourceLine
    ) {
    }
}
