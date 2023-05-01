package com.networknt.schema.regex;

import java.nio.charset.StandardCharsets;

import org.jcodings.specific.UTF8Encoding;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Syntax;

class JoniRegularExpression implements RegularExpression {
    private final Regex pattern;

    JoniRegularExpression(String regex) {
        // Joni is too liberal on some constructs
        String s = regex
            .replace("\\d", "[0-9]")
            .replace("\\D", "[^0-9]")
            .replace("\\w", "[a-zA-Z0-9_]")
            .replace("\\W", "[^a-zA-Z0-9_]")
            .replace("\\s", "[ \\f\\n\\r\\t\\v\\u00a0\\u1680\\u2000-\\u200a\\u2028\\u2029\\u202f\\u205f\\u3000\\ufeff]")
            .replace("\\S", "[^ \\f\\n\\r\\t\\v\\u00a0\\u1680\\u2000-\\u200a\\u2028\\u2029\\u202f\\u205f\\u3000\\ufeff]");

        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        this.pattern = new Regex(bytes, 0, bytes.length, Option.NONE, UTF8Encoding.INSTANCE, Syntax.ECMAScript);
    }

    @Override
    public boolean matches(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return this.pattern.matcher(bytes).search(0, bytes.length, Option.NONE) >= 0;
    }

}