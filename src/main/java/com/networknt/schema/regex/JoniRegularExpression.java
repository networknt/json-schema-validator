package com.networknt.schema.regex;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.jcodings.specific.UTF8Encoding;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Syntax;
import org.joni.constants.SyntaxProperties;
import org.joni.exception.SyntaxException;

/**
 * Joni {@link RegularExpression}.
 * <p>
 * This requires a dependency on org.jruby.joni:joni which along with its
 * dependency libraries are 2 MB.
 */
class JoniRegularExpression implements RegularExpression {
    private final Regex pattern;
    private final Pattern INVALID_ESCAPE_PATTERN = Pattern.compile(
            ".*\\\\([aeg-jl-moqyzACE-OQ-RT-VX-Z1-9]|k([^<]|$)|c$|[pP]([^{]|$)|u([^{0-9]|$)|x([0-9a-fA-F][^0-9a-fA-F]|[^0-9a-fA-F][0-9a-fA-F]|[^0-9a-fA-F][^0-9a-fA-F]|.?$)).*");
    
    /**
     * This is a custom syntax as Syntax.ECMAScript doesn't seem to be correct.
     * <p>
     * 
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Named_capturing_group">OP2_QMARK_LT_NAMED_GROUP</a>
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Named_backreference">OP2_ESC_K_NAMED_BACKREF</a>
     */
    private static final Syntax SYNTAX = new Syntax(Syntax.ECMAScript.name, Syntax.ECMAScript.op,
            Syntax.ECMAScript.op2 | SyntaxProperties.OP2_QMARK_LT_NAMED_GROUP
                    | SyntaxProperties.OP2_ESC_K_NAMED_BACKREF,
            Syntax.ECMAScript.op3, Syntax.ECMAScript.behavior, Syntax.ECMAScript.options,
            Syntax.ECMAScript.metaCharTable);

	  JoniRegularExpression(String regex) {
        this(regex, SYNTAX);
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
             * certain code points, but it is unable to distinguish \a vs \cG for instance as
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