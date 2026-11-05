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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private static String codePoints(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    @Test
    void allowsLettersNewerThanTheBundledTable() {
        // Adlam small letters (Unicode 9, Ll) are not in the Unicode 5.2 table.
        assertTrue(RFC5892.isValidALabel("xn--9d6hc"));
        assertTrue(RFC5892.isValid("xn--9d6hc.example"));
        assertTrue(RFC5892.isValid(codePoints(0x1E922, 0x1E923) + ".example"));
    }

    @Test
    void rejectsSymbolsUpperCaseAndControlsNewerThanTheBundledTable() {
        // U+1F600 and U+1F4A9 (emoji, So)
        assertFalse(RFC5892.isValidALabel("xn--e28h"));
        assertFalse(RFC5892.isValidALabel("xn--ls8h"));
        assertFalse(RFC5892.isValid("xn--ls8h.la"));
        // U+20BF BITCOIN SIGN (Sc)
        assertFalse(RFC5892.isValidALabel("xn--a-0qn"));
        // U+1C90 GEORGIAN MTAVRULI CAPITAL LETTER AN (Lu)
        assertFalse(RFC5892.isValidALabel("xn--j4f"));
        // U+1E900 ADLAM CAPITAL LETTER ALIF (Lu)
        assertFalse(RFC5892.isValidALabel("xn--bd6h"));
        // U+2066 LEFT-TO-RIGHT ISOLATE (Cf)
        assertFalse(RFC5892.isValidALabel("xn--11-88t"));
    }

    @Test
    void zeroWidthJoinersMayFollowAnyVirama() {
        // Devanagari, Sinhala and Malayalam ZWJ, Bengali ZWNJ
        assertTrue(RFC5892.isValidALabel("xn--11b2ezcw70k"));
        assertTrue(RFC5892.isValidALabel("xn--3zc0eybz95j"));
        assertTrue(RFC5892.isValidALabel("xn--bwca1om25e"));
        assertTrue(RFC5892.isValidALabel("xn--p5ba1o498f"));
        // A ZWJ after a consonant, not a virama
        assertFalse(RFC5892.isValid(codePoints(0x0D15, 0x200D, 0x0D15)));
    }

    @Test
    void hyphenPositionsAreCountedInCodePoints() {
        // "--" in the 3rd and 4th code points is invalid, whether or not the first two are supplementary.
        assertFalse(RFC5892.isValid(codePoints(0x4E00, 0x4E01, '-', '-', 'a')));
        assertFalse(RFC5892.isValid(codePoints(0x20000, 0x20001, '-', '-', 'a')));
        assertTrue(RFC5892.isValid(codePoints(0x20000, '-', '-', 'a')));
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

    @Test
    void appliesTheBidiRuleToALabelWithRightToLeftCharacters() {
        // Rule 1: a label with a Hebrew letter must not start with a digit.
        assertFalse(RFC5892.isValid("1\u05d0"));
        assertFalse(RFC5892.isValidALabel("xn--" + Punycode.encode("1\u05d0")));
        // Rule 4: European and Arabic-Indic digits must not both appear in an RTL label.
        assertFalse(RFC5892.isValid("\u06271\u0661"));
        assertTrue(RFC5892.isValid("\u05d01"));
        assertTrue(RFC5892.isValid("\u0627\u0661"));
    }

    @Test
    void cherokeeCapitalsStayValidAndItsSmallLettersAreNot() {
        // Cherokee case folding maps small letters (Unicode 8) to the capitals, which stay PVALID.
        assertTrue(RFC5892.isValid(codePoints(0x13A0, 0x13A1)));
        assertFalse(RFC5892.isValid(codePoints(0xAB70, 0xAB71)));
    }

    @Test
    void katakanaMiddleDotNeedsAKanaOrHanCharacterAnywhereInTheLabel() {
        // RFC 5892 A.7: any Hiragana, Katakana or Han character in the label, judged by script, not code block.
        assertTrue(RFC5892.isValid(codePoints(0x4E00, 0x30FB)));
        assertTrue(RFC5892.isValidALabel("xn--vek768f"));
        assertTrue(RFC5892.isValid(codePoints(0x30FB, 'a', 0x4E00)));
        assertFalse(RFC5892.isValid(codePoints(0x30FB, 0x30FC))); // PROLONGED SOUND MARK is Common, not Katakana
        assertFalse(RFC5892.isValidALabel("xn--vekc"));
    }

    @Test
    void greekKeraiaAndHebrewPunctuationAreJudgedByScript() {
        assertTrue(RFC5892.isValid(codePoints(0x0375, 0x1D26))); // GREEK LETTER SMALL CAPITAL GAMMA, outside the Greek blocks
        assertFalse(RFC5892.isValid(codePoints(0x03B1, 0x0375, 0x03E3))); // COPTIC SMALL LETTER SHEI, inside the Greek block
        assertFalse(RFC5892.isValidALabel("xn--wva3j0f"));
    }

    @Test
    void zeroWidthNonJoinerSkipsSupplementaryTransparentMarks() {
        // Adlam letters are dual joining and U+1E944 ADLAM ALIF LENGTHENER is transparent.
        assertTrue(RFC5892.isValid(codePoints(0x1E922, 0x1E944, 0x200C, 0x1E944, 0x1E923)));
        assertTrue(RFC5892.isValidALabel("xn--0ug1411pea4kb"));
        assertFalse(RFC5892.isValid(codePoints(0x1E922, 0x200C, 0x1E944, 0x1E944)));
    }

    @Test
    void followsTheUnicode60CategoryChangesListedInRfc6452() {
        assertTrue(RFC5892.isValid(codePoints(0x0CF1)));
        assertTrue(RFC5892.isValidALabel("xn--9uc"));
        assertFalse(RFC5892.isValid(codePoints('a', 0x19DA)));
    }

    @Test
    void rejectsNewCodePointsThatAreUnstableUnderCaseFoldingOrIgnorable() {
        assertFalse(RFC5892.isValid(codePoints('a', 0x1C80))); // CYRILLIC SMALL LETTER ROUNDED VE folds to U+0432
        assertFalse(RFC5892.isValidALabel("xn--a-wyl"));
        assertTrue(RFC5892.isValid(codePoints(0x13F5))); // CHEROKEE LETTER MV, a capital added in Unicode 8
    }

    @Test
    void rejectsTheDefaultIgnorableMongolianFreeVariationSelectorFour() {
        // On a JDK older than Unicode 14 the code point is unassigned, so the assertion would pass for the wrong reason.
        assumeTrue(Character.isDefined(0x180F));
        assertFalse(RFC5892.isValid(codePoints('a', 0x180F)));
    }

    @Test
    void viramaTableIsSortedForBinarySearch() throws Exception {
        java.lang.reflect.Field field = RFC5892.class.getDeclaredField("VIRAMAS");
        field.setAccessible(true);
        int[] viramas = (int[]) field.get(null);
        for (int n = 1; n < viramas.length; n++) assertTrue(viramas[n - 1] < viramas[n], "unsorted at index " + n);
    }

    @Test
    void zeroWidthJoinersMayFollowUnicode16Viramas() {
        assumeTrue(Character.isDefined(0x113CE)); // TULU-TIGALARI SIGN VIRAMA, Unicode 16
        assertTrue(RFC5892.isValid(codePoints(0x0915, 0x113CE, 0x200D, 0x0915)));
    }

    @Test
    void appliesTheBidiRuleToEveryLabelOfABidiDomainName() {
        // RFC 5893 rule 1: in a name with a right-to-left label, no label may start with a digit.
        assertFalse(RFC5892.isValid("1a.\u05D0\u05D1"));
        assertFalse(RFC5892.isValid("\u05D0\u05D1.1"));
        assertFalse(RFC5892.isValid("0a.\u05D0"));
        // Rule 6: a left-to-right label may end with a digit.
        assertTrue(RFC5892.isValid("\u05D0\u05D1.a1"));
        assertTrue(RFC5892.isValid("\u05D0\u05D1.example1"));
        // A name without right-to-left labels is not a Bidi domain name.
        assertTrue(RFC5892.isValid("1a.b\u00FCcher"));
        assertFalse(RFC5892.satisfiesBidiDomainRule(new String[] {"1a", "xn--4dbc"}));
        assertTrue(RFC5892.satisfiesBidiDomainRule(new String[] {"a1", "xn--4dbc"}));
    }

    @Test
    void loadsTheUnicodeTablesSafelyOnConcurrentFirstUse() throws Exception {
        URL classes = RFC5892.class.getProtectionDomain().getCodeSource().getLocation();
        for (int round = 0; round < 20; round++) {
            // A fresh copy of the utils classes, so the lazily loaded tables start empty in every round.
            try (URLClassLoader loader = new ChildFirstLoader(classes)) {
                Method isValid = loader.loadClass(RFC5892.class.getName()).getMethod("isValid", String.class);
                int threads = 8;
                CountDownLatch start = new CountDownLatch(1);
                ExecutorService pool = Executors.newFixedThreadPool(threads);
                try {
                    List<Future<Object>> results = new ArrayList<>();
                    for (int t = 0; t < threads; t++) {
                        // A ZWNJ between two dual-joining Arabic letters needs both the IDNA and the joining-type tables.
                        String name = t % 2 == 0 ? "\u0628\u200C\u0628" : "b\u00FCcher.\u0628\u200C\u0628";
                        results.add(pool.submit(() -> {
                            start.await();
                            return isValid.invoke(null, name);
                        }));
                    }
                    start.countDown();
                    for (Future<Object> result : results) assertEquals(Boolean.TRUE, result.get());
                } finally {
                    pool.shutdownNow();
                }
            }
        }
    }

    /** Loads the utils package itself and delegates everything else, such as the bundled tables' anchor class. */
    private static final class ChildFirstLoader extends URLClassLoader {
        ChildFirstLoader(URL classes) {
            super(new URL[] {classes}, RFC5892Test.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith("com.networknt.schema.utils.")) return super.loadClass(name, resolve);
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                return null != c ? c : findClass(name);
            }
        }
    }
}
