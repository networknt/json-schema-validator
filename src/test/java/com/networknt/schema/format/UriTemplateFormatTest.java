/*
 * Copyright (c) 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UriTemplateFormatTest {

    private static final Format FORMAT = Formats.DEFAULT.stream()
            .filter(format -> "uri-template".equals(format.getName()))
            .findFirst()
            .orElseThrow(IllegalStateException::new);

    private static boolean matches(String value) {
        return FORMAT.matches(null, value);
    }

    /**
     * The literals of RFC 6570 section 2.1, written out from the RFC's ABNF, plus the apostrophe that the
     * JSON-Schema-Test-Suite accepts. {@code %} is excluded because it only starts a pct-encoded triplet.
     */
    private static boolean isLiteral(int cp) {
        if (cp < 0x80) {
            return cp == 0x21 || cp == 0x23 || cp == 0x24 || cp == 0x26 || cp == 0x27
                    || (0x28 <= cp && cp <= 0x3B) || cp == 0x3D || (0x3F <= cp && cp <= 0x5B)
                    || cp == 0x5D || cp == 0x5F || (0x61 <= cp && cp <= 0x7A) || cp == 0x7E;
        }
        // ucschar
        if ((0xA0 <= cp && cp <= 0xD7FF) || (0xF900 <= cp && cp <= 0xFDCF) || (0xFDF0 <= cp && cp <= 0xFFEF)) {
            return true;
        }
        if (0x10000 <= cp && cp <= 0xDFFFF) {
            return (cp & 0xFFFF) <= 0xFFFD;
        }
        if (0xE1000 <= cp && cp <= 0xEFFFD) {
            return true;
        }
        // iprivate
        return (0xE000 <= cp && cp <= 0xF8FF) || (0xF0000 <= cp && cp <= 0xFFFFD)
                || (0x100000 <= cp && cp <= 0x10FFFD);
    }

    @Test
    void literalsMatchRfc6570ForEveryCodePoint() {
        for (int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
            if (cp == '%' || cp == '{' || cp == '}') {
                continue; // these start a pct-encoded triplet or an expression
            }
            String value = "a" + new String(Character.toChars(cp)) + "b";
            assertEquals(isLiteral(cp), matches(value), () -> "U+" + Integer.toHexString(value.codePointAt(1)));
        }
    }

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Test
    void longTemplatesDoNotOverflowTheStack() {
        // Each of these threw StackOverflowError when the pattern recursed once per character or escape.
        assertTrue(matches(repeat("a", 1_000_000)));
        assertTrue(matches(repeat("a\u00e9\ud83d\ude00", 100_000))); // ASCII, BMP and supplementary runs
        assertTrue(matches("{" + repeat("a", 1_000_000) + "}"));
        assertTrue(matches("{" + repeat("a.", 100_000) + "a}"));
        assertTrue(matches("{" + repeat("%41", 100_000) + "}"));
        assertTrue(matches("{" + repeat("a,", 100_000) + "a}"));
        assertTrue(matches(repeat("%20", 100_000)));
        assertTrue(matches(repeat("{a}", 100_000)));
        assertTrue(matches(repeat("ab{c}%20", 100_000)));
        assertFalse(matches(repeat("a", 1_000_000) + " "));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "a b", // space
        "a\\b",
        "a\u0080b", // C1 controls
        "a\u0085b",
        "a\ud800b", // lone surrogates
        "a\udc00b",
        "a\ufdd0b", // noncharacters
        "a\ufffeb",
        "a\uffffb",
        "a\ud83f\udffeb", // U+1FFFE, a noncharacter
        "a\ufffdb", // U+FFFD, in the Specials block outside ucschar
        "a\udb40\udc41b", // U+E0041, a tag character
        "a\udb40\udd00b", // U+E0100, a variation selector
        "a{b", // a brace or percent sign outside an expression or pct-encoded triplet
        "a}b",
        "a%b",
        "a%2",
        "a%g0",
        "{",
        "}",
        "{}",
        "{a",
        "{a b}"
    })
    void rejects(String value) {
        assertFalse(matches(value), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "a'b",
        "a%20b%5Cc",
        "http://example.com/{id}",
        "a\u00e9b", // ucschar
        "a\ue000b", // iprivate in the BMP
        "a\ud83d\ude00b", // U+1F600 in plane 1
        "a\udb44\udc00b", // U+E1000, the start of ucschar in plane 14
        "a\udb80\udc00b", // U+F0000, iprivate
        "a\udbff\udffdb" // U+10FFFD, iprivate
    })
    void accepts(String value) {
        assertTrue(matches(value), value);
    }
}
