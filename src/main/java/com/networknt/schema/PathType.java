package com.networknt.schema;

import java.util.function.Function;
import java.util.function.IntPredicate;

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

        if (token.isEmpty()) {
            throw new IllegalArgumentException("A JSONPath selector cannot be empty");
        }

        String t = token;
        /*
         * Accepted characters for shorthand paths:
         * - 'a' through 'z'
         * - 'A' through 'Z'
         * - '0' through '9'
         * - Underscore ('_')
         * - any non-ASCII Unicode character
         */
        if (JSONPath.isShorthand(t)) {
            return "." + t;
        }

        boolean containsApostrophe = 0 <= t.indexOf('\'');
        if (containsApostrophe) {
            // Make sure also any apostrophes are escaped.
            t = t.replace("'", "\\'");
        }

        return "['" + t + "']";
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
        return currentPath + this.appendTokenFn.apply(child);
    }

    /**
     * Append the given index to the provided current path.
     *
     * @param currentPath The path to append to.
     * @param index The index to append.
     * @return The resulting complete path.
     */
    public String append(String currentPath, int index) {
        return currentPath + this.appendIndexFn.apply(index);
    }

    /**
     * Return the representation of the document root.
     *
     * @return The root token.
     */
    public String getRoot() {
        return this.rootToken;
    }

    public String convertToJsonPointer(String path) {
        switch (this) {
            case JSON_POINTER: return path;
            case JSON_PATH: return fromJsonPath(path);
            default: return fromLegacy(path);
        }
    }

    static String fromLegacy(String path) {
        return path
            .replace("\"", "")
            .replace("]", "")
            .replace('[', '/')
            .replace('.', '/')
            .replace("$", "");
    }

    static String fromJsonPath(String str) {
        if (null == str || str.isEmpty() || '$' != str.charAt(0)) { 
            throw new IllegalArgumentException("JSON Path must start with '$'");
        }

        String tail = str.substring(1);
        if (tail.isEmpty()) {
            return "";
        }

        int len = tail.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len;) {
            char c = tail.charAt(i);
            switch (c) {
                case '.': sb.append('/'); i = parseShorthand(sb, tail, i + 1); break;
                case '[': sb.append('/'); i = parseSelector(sb, tail, i + 1); break;
                default: throw new IllegalArgumentException("JSONPath must reference a property or array index");
            }
        }
        return sb.toString();
    }

    /**
     * Parses a JSONPath shorthand selector
     * @param sb receives the result
     * @param s the source string
     * @param pos the index into s immediately following the dot
     * @return the index following the selector name
     */
    static int parseShorthand(StringBuilder sb, String s, int pos) {
        int len = s.length();
        int i = pos;
        for (; i < len; ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '.':
                case '[':
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return i;
    }

    /**
     * Parses a JSONPath selector
     * @param sb receives the result
     * @param s the source string
     * @param pos the index into s immediately following the open bracket
     * @return the index following the closing bracket
     */
    static int parseSelector(StringBuilder sb, String s, int pos) {
        int close = s.indexOf(']', pos);
        if (-1 == close) {
            throw new IllegalArgumentException("JSONPath contains an unterminated selector");
        }

        if ('\'' == s.charAt(pos)) {
            parseQuote(sb, s, pos + 1);
        } else {
            sb.append(s.substring(pos, close));
        }

        return close + 1;
    }

    /**
     * Parses a single-quoted string.
     * @param sb receives the result
     * @param s the source string
     * @param pos the index into s immediately following the open quote
     * @return the index following the closing quote
     */
    static int parseQuote(StringBuilder sb, String s, int pos) {
        int close = pos;
        do {
            close = s.indexOf('\'', close);
            if (-1 == close) {
                throw new IllegalArgumentException("JSONPath contains an unterminated quoted string");
            }
        } while ('\\' == s.charAt(close - 1)) ;
        sb.append(s.substring(pos, close));
        return close + 1;
    }

    static class JSONPath {
        public static final IntPredicate ALPHA = c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        public static final IntPredicate DIGIT = c -> c >= '0' && c <= '9';
        public static final IntPredicate NON_ASCII = c -> (c >= 0x80 && c <= 0x10FFFF);
        public static final IntPredicate UNDERSCORE = c -> '_' == c;

        public static final IntPredicate NAME_FIRST = ALPHA.or(UNDERSCORE).or(NON_ASCII);
        public static final IntPredicate NAME_CHAR = NAME_FIRST.or(DIGIT);

        public static boolean isShorthand(String selector) {
            if (null == selector || selector.isEmpty()) {
                throw new IllegalArgumentException("A JSONPath selector cannot be empty");
            }

            /*
             * Accepted characters for shorthand paths:
             * - 'a' through 'z'
             * - 'A' through 'Z'
             * - '0' through '9'
             * - Underscore ('_')
             * - any non-ASCII Unicode character
             */
            return NAME_FIRST.test(selector.codePointAt(0)) && selector.codePoints().skip(1).allMatch(NAME_CHAR);
        }
    }
}
