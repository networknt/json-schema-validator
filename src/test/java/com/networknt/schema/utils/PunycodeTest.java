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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PunycodeTest {

    private static String codePoints(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    /** Sample strings from RFC 3492 section 7.1, plus the common "bücher" example. */
    static Stream<Arguments> rfc3492Samples() {
        return Stream.of(
            // (A) Arabic (Egyptian)
            Arguments.of(codePoints(0x0644, 0x064A, 0x0647, 0x0645, 0x0627, 0x0628, 0x062A, 0x0643, 0x0644,
                0x0645, 0x0648, 0x0634, 0x0639, 0x0631, 0x0628, 0x064A, 0x061F), "egbpdaj6bu4bxfgehfvwxn"),
            // (B) Chinese (simplified)
            Arguments.of(codePoints(0x4ED6, 0x4EEC, 0x4E3A, 0x4EC0, 0x4E48, 0x4E0D, 0x8BF4, 0x4E2D, 0x6587),
                "ihqwcrb4cv8a8dqg056pqjye"),
            // (L) 3<nen>B<gumi><kinpachi><sensei>, mixed case basic code points
            Arguments.of(codePoints(0x0033, 0x5E74, 0x0042, 0x7D44, 0x91D1, 0x516B, 0x5148, 0x751F),
                "3B-ww4c5e180e575a65lsy2b"),
            // (R) <sono><supiido><de>
            Arguments.of(codePoints(0x305D, 0x306E, 0x30B9, 0x30D4, 0x30FC, 0x30C9, 0x3067), "d9juau41awczczp"),
            // (S) -> $1.00 <-, basic code points only
            Arguments.of("-> $1.00 <-", "-> $1.00 <--"),
            Arguments.of("bücher", "bcher-kva"),
            // A supplementary-plane code point (U+1F4A9)
            Arguments.of(codePoints(0x1F4A9), "ls8h"));
    }

    @ParameterizedTest
    @MethodSource("rfc3492Samples")
    void decodes(String unicode, String punycode) {
        assertEquals(unicode, Punycode.decode(punycode));
    }

    @ParameterizedTest
    @MethodSource("rfc3492Samples")
    void encodes(String unicode, String punycode) {
        assertEquals(punycode, Punycode.encode(unicode));
    }

    @Test
    void rejectsOverflow() {
        assertNull(Punycode.decode("99999999999"));
    }

    @Test
    void rejectsSurrogatesAndCodePointsAboveTheUnicodeRange() {
        assertNull(Punycode.decode("ib9b")); // U+D800, a lone surrogate
        assertNull(Punycode.decode("bb00h")); // above U+10FFFF without overflowing an int
    }

    @Test
    void rejectsTruncatedInput() {
        // "k" is a digit that needs a following digit to complete the variable-length integer.
        assertNull(Punycode.decode("bcher-k"));
    }

    @Test
    void readsDelimiterAtIndexZeroAsADigit() {
        // With no basic code points before it, the delimiter is not consumed, and "-" is not a digit.
        assertNull(Punycode.decode("-kva"));
    }

    @Test
    void rejectsNonBasicCodePointsBeforeTheDelimiter() {
        assertNull(Punycode.decode("bücher-kva"));
    }

    @Test
    void rejectsNonDigitsAfterTheDelimiter() {
        assertNull(Punycode.decode("bcher-kvä"));
    }
}
