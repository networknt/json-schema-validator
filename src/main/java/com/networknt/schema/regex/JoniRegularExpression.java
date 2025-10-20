package com.networknt.schema.regex;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.jcodings.ApplyAllCaseFoldFunction;
import org.jcodings.CaseFoldCodeItem;
import org.jcodings.CodeRange;
import org.jcodings.Encoding;
import org.jcodings.IntHolder;
import org.jcodings.constants.CharacterType;
import org.jcodings.specific.UTF8Encoding;
import org.jcodings.unicode.UnicodeCodeRange;
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
        regex = RegularExpressions.replaceDollarAnchors(regex);
        byte[] bytes = regex.getBytes(StandardCharsets.UTF_8);
        this.pattern = new Regex(bytes, 0, bytes.length, Option.SINGLELINE, ECMAScriptUTF8Encoding.INSTANCE, syntax);
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

    static class Arrays {
        public static boolean equals(byte[] a, byte[] a2, int p, int end) {
            if (a==a2) {
                return true;
            }
            if (a==null || a2==null) {
                return false;
            }

            int length = a.length;
            if ((end - p) != length) {
                return false;
            }

            for (int i=0; i<length; i++) {
                if (a[i] != a2[i+p]) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * An {@link Encoding} that returns the appropriate code ranges that correspond
     * to the ECMA-262 regular expression implementation instead of matching
     * directly to a Unicode General Category.
     */
    public static class ECMAScriptUTF8Encoding extends DelegatingEncoding {
        /*
         * [0-9]
         */
        private static final int[] CR_DIGIT = { 1, '0', '9' };
        /*
         * [a-zA-Z0-9_]
         */
        private static final int[] CR_WORD = { 4, '0', '9', 'A', 'Z', '_', '_', 'a', 'z' };
        /*
         * [\f\n\r\t\v\u0020\u00a0\u1680\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff]
         */
        private static final int[] CR_SPACE = { 10, '\t', '\r', ' ', ' ', '\u00a0', '\u00a0', '\u1680', '\u1680', '\u2000',
                '\u200a', '\u2028', '\u2029', '\u202f', '\u202f', '\u205f', '\u205f', '\u3000', '\u3000', '\ufeff',
                '\ufeff' };
        /*
         * For \p{digit}
         */
        private static final byte[] PROPERTY_NAME_DIGIT = { 100, 105, 103, 105, 116}; 

        public static final ECMAScriptUTF8Encoding INSTANCE = new ECMAScriptUTF8Encoding();

        protected ECMAScriptUTF8Encoding() {
            super(UTF8Encoding.INSTANCE);
        }

        @Override
        public int[] ctypeCodeRange(int ctype, IntHolder sbOut) {
            switch (ctype) {
            case CharacterType.DIGIT: // \d
                sbOut.value = 0x80;
                return CR_DIGIT;
            case CharacterType.WORD: // \w
                sbOut.value = 0x80;
                return CR_WORD;
            case CharacterType.SPACE: // \s
                sbOut.value = 0x80;
                return CR_SPACE;
            }
            return delegate.ctypeCodeRange(ctype, sbOut);
        }

        @Override
        public boolean isCodeCType(int code, int ctype) {
            switch (ctype) {
            case CharacterType.DIGIT: // \d
                return CodeRange.isInCodeRange(CR_DIGIT, code); 
            case CharacterType.WORD: // \w
                return CodeRange.isInCodeRange(CR_WORD, code); 
            case CharacterType.SPACE: // \s
                return CodeRange.isInCodeRange(CR_SPACE, code); 
            }
            return delegate.isCodeCType(code, ctype);
        }

        @Override
        public int propertyNameToCType(byte[]name, int p, int end) {
            if (Arrays.equals(PROPERTY_NAME_DIGIT, name, p, end)) {
                return UnicodeCodeRange.ND.ordinal();// 55 Same as \p{Nd} and not returning CharacterType.DIGIT
            }
            return delegate.propertyNameToCType(name, p, end);
        }
    }

    /**
     * An {@link Encoding} that delegates to another {@link Encoding}.
     * <p>
     * This can be used to customize the behavior of implementations that are final.
     */
    public static class DelegatingEncoding extends Encoding {
        protected final Encoding delegate;
        protected DelegatingEncoding(Encoding delegate) {
            super(new String(delegate.getName()), delegate.minLength(), delegate.maxLength());
            this.delegate = delegate;
        }
        @Override
        public Charset getCharset() {
            return delegate.getCharset();
        }
        @Override
        public String getCharsetName() {
            return delegate.getCharsetName();
        }
        @Override
        public int length(byte c) {
            return delegate.length(c);
        }
        @Override
        public int length(byte[] bytes, int p, int end) {
            return delegate.length(bytes, p, end);
        }
        @Override
        public boolean isNewLine(byte[] bytes, int p, int end) {
            return delegate.isNewLine(bytes, p, end);
        }
        @Override
        public int mbcToCode(byte[] bytes, int p, int end) {
            return delegate.mbcToCode(bytes, p, end);
        }
        @Override
        public int codeToMbcLength(int code) {
            return delegate.codeToMbcLength(code);
        }
        @Override
        public int codeToMbc(int code, byte[] bytes, int p) {
            return delegate.codeToMbc(code, bytes, p);
        }
        @Override
        public int mbcCaseFold(int flag, byte[] bytes, IntHolder pp, int end, byte[] to) {
            return delegate.mbcCaseFold(flag, bytes, pp, end, to);
        }
        @Override
        public byte[] toLowerCaseTable() {
            return delegate.toLowerCaseTable();
        }
        @Override
        public void applyAllCaseFold(int flag, ApplyAllCaseFoldFunction fun, Object arg) {
            delegate.applyAllCaseFold(flag, fun, arg);
        }
        @Override
        public CaseFoldCodeItem[] caseFoldCodesByString(int flag, byte[] bytes, int p, int end) {
            return delegate.caseFoldCodesByString(flag, bytes, p, end);
        }
        @Override
        public int propertyNameToCType(byte[] bytes, int p, int end) {
            return delegate.propertyNameToCType(bytes, p, end);
        }
        @Override
        public boolean isCodeCType(int code, int ctype) {
            return delegate.isCodeCType(code, ctype);
        }
        @Override
        public int[] ctypeCodeRange(int ctype, IntHolder sbOut) {
            return delegate.ctypeCodeRange(ctype, sbOut);
        }
        @Override
        public int leftAdjustCharHead(byte[] bytes, int p, int s, int end) {
            return delegate.leftAdjustCharHead(bytes, p, s, end);
        }
        @Override
        public boolean isReverseMatchAllowed(byte[] bytes, int p, int end) {
            return delegate.isReverseMatchAllowed(bytes, p, end);
        }
        @Override
        public int caseMap(IntHolder flagP, byte[] bytes, IntHolder pp, int end, byte[] to, int toP, int toEnd) {
            return delegate.caseMap(flagP, bytes, pp, end, to, toP, toEnd);
        }
        @Override
        public int strLength(byte[] bytes, int p, int end) {
            return delegate.strLength(bytes, p, end);
        }
        @Override
        public int strCodeAt(byte[] bytes, int p, int end, int index) {
            return delegate.strCodeAt(bytes, p, end, index);
        }
        @Override
        public boolean isMbcCrnl(byte[] bytes, int p, int end) {
            return delegate.isMbcCrnl(bytes, p, end);
        }
    }
}