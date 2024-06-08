package com.networknt.schema.regex;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.jcodings.specific.UTF8Encoding;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Syntax;
import org.joni.exception.SyntaxException;

/**
 * ECMAScript {@link RegularExpression}.
 */
class JoniRegularExpression implements RegularExpression {
    private final Regex pattern;
    private final Pattern INVALID_ESCAPE_PATTERN = Pattern.compile(
            ".*\\\\([aeg-moqyzACE-OQ-RT-VX-Z1-9]|c$|[pP]([^{]|$)|u([^{0-9]|$)|x([0-9a-fA-F][^0-9a-fA-F]|[^0-9a-fA-F][0-9a-fA-F]|[^0-9a-fA-F][^0-9a-fA-F]|.?$)).*");

    JoniRegularExpression(String regex) {
        this(regex, Syntax.ECMAScript);
    }

    JoniRegularExpression(String regex, Syntax syntax) {
        validate(regex);
        // Joni is too liberal on some constructs
        String s = regex
            .replace("\\d", "[0-9]")
            .replace("\\D", "[^0-9]")
            .replace("\\w", "[a-zA-Z0-9_]")
            .replace("\\W", "[^a-zA-Z0-9_]")
            .replace("\\s", "[ \\f\\n\\r\\t\\v\\u00a0\\u1680\\u2000-\\u200a\\u2028\\u2029\\u202f\\u205f\\u3000\\ufeff]")
            .replace("\\S", "[^ \\f\\n\\r\\t\\v\\u00a0\\u1680\\u2000-\\u200a\\u2028\\u2029\\u202f\\u205f\\u3000\\ufeff]");

        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        this.pattern = new Regex(bytes, 0, bytes.length, Option.SINGLELINE, UTF8Encoding.INSTANCE, syntax);
    }

    protected void validate(String regex) {
        // Joni is not strict with escapes
        if (INVALID_ESCAPE_PATTERN.matcher(regex).matches()) {
            /*
             * One option considered was a custom Encoding implementation that rejects
             * certain code points but it is unable to distinguish \a vs \cG for instance as
             * both translate to BEL
             */
            throw new SyntaxException("Invalid escape");
        }
    }

    @Override
    public boolean matches(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return this.pattern.matcher(bytes).search(0, bytes.length, Option.NONE) >= 0;
    }

}