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
package com.networknt.schema.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@code idn-hostname} rules in {@link RFC5892#isValid(String)} and the {@code hostname}
 * A-label rules in {@link RFC5892#isValidALabel(String)}.
 */
class RFC5892Test {

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    @Test
    void uLabelAndItsALabelAgree() {
        // 40 x U+00DF is a 46-character A-label: both forms are within the 63-character limit.
        String uLabel = repeat("ß", 40);
        String aLabel = "xn--" + Punycode.encode(uLabel);
        assertTrue(aLabel.length() <= 63);
        assertTrue(RFC5892.isValid(uLabel));
        assertTrue(RFC5892.isValid(aLabel));
    }

    @Test
    void uLabelLengthIsMeasuredInALabelForm() {
        // 60 code points, but the A-label is longer than 63 characters.
        String uLabel = repeat("äb", 30);
        assertTrue(("xn--" + Punycode.encode(uLabel)).length() > 63);
        assertFalse(RFC5892.isValid(uLabel));
    }

    @Test
    void aLabelsAreCaseInsensitive() {
        assertTrue(RFC5892.isValid("xn--bcher-kva.example"));
        assertTrue(RFC5892.isValid("XN--BCHER-KVA.example"));
        assertTrue(RFC5892.isValid("xn--Bcher-kva.example"));
        assertTrue(RFC5892.isValidALabel("XN--BCHER-KVA"));
        assertTrue(RFC5892.isValidALabel("xn--Bcher-kva"));
    }

    @Test
    void overallLengthIsLimitedTo253Characters() {
        String label = repeat("a", 63);
        String name253 = label + "." + label + "." + label + "." + repeat("a", 61);
        assertTrue(RFC5892.isValid(name253));
        assertFalse(RFC5892.isValid(name253 + "a"));
    }

    @Test
    void labelLongerThan63CharactersIsRejectedBeforeDecoding() {
        assertFalse(RFC5892.isValid("xn--" + repeat("a", 60)));
        assertFalse(RFC5892.isValidALabel("xn--" + repeat("a", 60)));
        // A very long label is rejected by its length alone.
        assertFalse(RFC5892.isValid("xn--" + repeat("99", 100_000)));
    }

    @Test
    void aLabelMustRoundTrip() {
        assertFalse(RFC5892.isValidALabel("xn--X"));
        // Decodes to ASCII only.
        assertFalse(RFC5892.isValidALabel("xn--abc-"));
    }

    @Test
    void allowsCodePointsNewerThanTheBundledTable() {
        // U+1F4A9, U+1F600 and Adlam (Unicode 9) are not in the RFC 5892 Appendix B table.
        assertTrue(RFC5892.isValidALabel("xn--ls8h"));
        assertTrue(RFC5892.isValidALabel("xn--e28h"));
        assertTrue(RFC5892.isValidALabel("xn--bd6h"));
        assertTrue(RFC5892.isValidALabel("xn--9d6hc"));
        // idn-hostname applies the same policy, so it accepts what hostname accepts.
        assertTrue(RFC5892.isValid("xn--ls8h.la"));
        assertTrue(RFC5892.isValid(new String(Character.toChars(0x1F4A9)) + ".la"));
    }

    @Test
    void hostnameALabelsStillApplyTheIdnaRules() {
        // Leading combining mark
        assertFalse(RFC5892.isValidALabel("xn--hello-txk"));
        // Disallowed U+302E
        assertFalse(RFC5892.isValidALabel("xn--07jt112bpxg"));
        // ZERO WIDTH JOINER not preceded by anything
        assertFalse(RFC5892.isValidALabel("xn--02b508i"));
        // ZERO WIDTH JOINER preceded by a virama
        assertTrue(RFC5892.isValidALabel("xn--11b2ezcw70k"));
    }
}
