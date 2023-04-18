package com.networknt.schema;

import java.util.function.Function;

/**
 * Enumeration defining the different approached available to generate the paths added to validation messages.
 */
public enum PathType {

    /**
     * The legacy approach, loosely based on JSONPath (but not guaranteed to give valid JSONPath expressions).
     */
    LEGACY("$", (token) -> "." + token, (index) -> "[" + index + "]"),
    /**
     * Paths as JSONPath expressions.
     */
    JSON_PATH("$", (token) -> {
        /*
         * Accepted characters for shorthand paths:
         * - 'a' through 'z'
         * - 'A' through 'Z'
         * - '0' through '9'
         * - Underscore ('_')
         */
        if (token.codePoints().allMatch(c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')) {
            return "." + token;
        } else {
            return "[\"" + token + "\"]";
        }
    }, (index) -> "[" + index + "]"),
    /**
     * Paths as JSONPointer expressions.
     */
    JSON_POINTER("", (token) -> {
        /*
         * Escape '~' with '~0' and '/' with '~1'.
         */
        if (token.indexOf('~') != -1) {
            token = token.replace("~", "~0");
        }
        if (token.indexOf('/') != -1) {
            token = token.replace("/", "~1");
        }
        return "/" + token;
    }, (index) -> "/" + index);

    /**
     * The default path generation approach to use.
     */
    public static final PathType DEFAULT = LEGACY;
    private final String rootToken;
    private final Function<String, String> appendTokenFn;
    private final Function<Integer, String> appendIndexFn;

    /**
     * Constructor.
     *
     * @param rootToken The token representing the document root.
     * @param appendTokenFn A function used to define the path fragment used to append a token (e.g. property) to an existing path.
     * @param appendIndexFn A function used to append an index (for arrays) to an existing path.
     */
    PathType(String rootToken, Function<String, String> appendTokenFn, Function<Integer, String> appendIndexFn) {
        this.rootToken = rootToken;
        this.appendTokenFn = appendTokenFn;
        this.appendIndexFn = appendIndexFn;
    }

    /**
     * Append the given child token to the provided current path.
     *
     * @param currentPath The path to append to.
     * @param child The child token.
     * @return The resulting complete path.
     */
    public String append(String currentPath, String child) {
        return currentPath + appendTokenFn.apply(child);
    }

    /**
     * Append the given index to the provided current path.
     *
     * @param currentPath The path to append to.
     * @param index The index to append.
     * @return The resulting complete path.
     */
    public String append(String currentPath, int index) {
        return currentPath + appendIndexFn.apply(index);
    }

    /**
     * Return the representation of the document root.
     *
     * @return The root token.
     */
    public String getRoot() {
        return rootToken;
    }

    public String convertToJsonPointer(String path) {
        switch (this) {
            case JSON_POINTER: return path;
            default: return fromLegacyOrJsonPath(path);
        }
    }

    static String fromLegacyOrJsonPath(String path) {
        return path
            .replace("\"", "")
            .replace("]", "")
            .replace('[', '/')
            .replace('.', '/')
            .replace("$", "");
    }
}
