package com.networknt.schema.utils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Locale;
import java.util.function.BiPredicate;

import static com.networknt.schema.utils.UnicodeDatabase.*;
import static java.lang.Character.*;

/**
 * Encapsulates the rules determining whether a label conforms to the RFC 5892 specification.
 * <p>
 * In the context of RFC 5892. a label is a subcomponent of a DNS entry. For example,
 * schema.networknt.com has three sub-components or labels: com, networknt and schema.
 * <p>
 * Each component (or label) must satisfy the constraints identified in RFC 5892.
 */
public class RFC5892 {

    private static final String ACE_PREFIX = "xn--";
    private static final int ACE_PREFIX_LENGTH = ACE_PREFIX.length();
    private static final int MAX_LABEL_LENGTH = 63;
    private static final int MAX_NAME_LENGTH = 253;

    private static final int GREEK_LOWER_NUMERAL_SIGN = 0x0375;
    private static final int HEBREW_GERESH = 0x05F3;
    private static final int HEBREW_GERSHAYIM = 0x05F4;
    private static final int KATAKANA_MIDDLE_DOT = 0x30FB;
    private static final int MIDDLE_DOT = 0x00B7;
    /**
     * Code points with Canonical_Combining_Class Virama (9), from Unicode 16.0.0. Sorted, for binary search.
     */
    private static final int[] VIRAMAS = {
        0x094D, 0x09CD, 0x0A4D, 0x0ACD, 0x0B4D, 0x0BCD, 0x0C4D, 0x0CCD, 0x0D3B, 0x0D3C, 0x0D4D, 0x0DCA,
        0x0E3A, 0x0EBA, 0x0F84, 0x1039, 0x103A, 0x1714, 0x1715, 0x1734, 0x17D2, 0x1A60, 0x1B44, 0x1BAA,
        0x1BAB, 0x1BF2, 0x1BF3, 0x2D7F, 0xA806, 0xA82C, 0xA8C4, 0xA953, 0xA9C0, 0xAAF6, 0xABED, 0x10A3F,
        0x11046, 0x11070, 0x1107F, 0x110B9, 0x11133, 0x11134, 0x111C0, 0x11235, 0x112EA, 0x1134D, 0x113CE,
        0x113CF, 0x113D0, 0x11442, 0x114C2, 0x115BF, 0x1163F, 0x116B6, 0x1172B, 0x11839, 0x1193D, 0x1193E,
        0x119E0, 0x11A34, 0x11A47, 0x11A99, 0x11C3F, 0x11D44, 0x11D45, 0x11D97, 0x11F41, 0x11F42, 0x1612F
    };
    private static final int ZERO_WIDTH_JOINER = 0x200D;
    private static final int ZERO_WIDTH_NON_JOINER = 0x200C;

    private static final BitSet CONTEXTJ = new BitSet(0x110000);
    private static final BitSet CONTEXTO = new BitSet(0x110000);
    private static final BitSet DISALLOWED = new BitSet(0x110000);
    private static final BitSet UNASSIGNED = new BitSet(0x110000);
    private static volatile boolean loaded;

    private static final BiPredicate<String, Integer> RULE_ARABIC_INDIC_DIGITS_RULE = RFC5892::testArabicIndicDigit;
    private static final BiPredicate<String, Integer> RULE_EXTENDED_ARABIC_INDIC_DIGITS_RULE = RFC5892::testExtendedArabicIndicDigit;
    private static final BiPredicate<String, Integer> RULE_GREEK_LOWER_NUMERAL_SIGN = RFC5892::testGreekLowerNumeralSign;
    private static final BiPredicate<String, Integer> RULE_HEBREW_GERESH_GERSHAYIM = RFC5892::testHebrewPuncuation;
    private static final BiPredicate<String, Integer> RULE_KATAKANA_MIDDLE_DOT = RFC5892::testKatakanaMiddleDot;
    private static final BiPredicate<String, Integer> RULE_MIDDLE_DOT = RFC5892::testeMiddleDotRule;
    private static final BiPredicate<String, Integer> RULE_ZERO_WIDTH_JOINER = RFC5892::testZeroWidthJoiner;
    private static final BiPredicate<String, Integer> RULE_ZERO_WIDTH_NON_JOINER = RFC5892::testZeroWidthNonJoiner;

    private static final BiPredicate<String, Integer> ALLOWED_CHARACTER = RFC5892::testAllowedCharacter;

    private static final BiPredicate<String, Integer> LTR = RFC5892::testLTR;
    private static final BiPredicate<String, Integer> RTL = RFC5892::testRTL;

    private static final BiPredicate<String, Integer> IDNA_RULES =
        ALLOWED_CHARACTER
            .and(RULE_ARABIC_INDIC_DIGITS_RULE)
            .and(RULE_EXTENDED_ARABIC_INDIC_DIGITS_RULE)
            .and(RULE_GREEK_LOWER_NUMERAL_SIGN)
            .and(RULE_HEBREW_GERESH_GERSHAYIM)
            .and(RULE_KATAKANA_MIDDLE_DOT)
            .and(RULE_MIDDLE_DOT)
            .and(RULE_ZERO_WIDTH_JOINER)
            .and(RULE_ZERO_WIDTH_NON_JOINER);

    private static boolean isContextJ(int codepoint) {
        if (!loaded) loadDerivedProperties();
        return CONTEXTJ.get(codepoint);
    }

    private static boolean isContextO(int codepoint) {
        if (!loaded) loadDerivedProperties();
        return CONTEXTO.get(codepoint);
    }

    private static boolean isDisallowed(int codepoint) {
        if (!loaded) loadDerivedProperties();
        return DISALLOWED.get(codepoint);
    }

    private static boolean isUnassigned(int codepoint) {
        if (!loaded) loadDerivedProperties();
        return UNASSIGNED.get(codepoint);
    }

    private static boolean testAllowedCharacter(String s, int i) {
        int c = s.codePointAt(i);
        return isPermitted(c) // RFC 5891 4.2.2.  Rejection of Characters That Are Not Permitted
            && !isContextJ(c)   && !isContextO(c);  // RFC 5891 4.2.3.3.  Contextual Rules
    }

    private static boolean isPermitted(int c) {
        switch (c) {
            // RFC 6452: Unicode 6.0 changed the general category of these three, and IDNA2008 kept the newly
            // derived values instead of adding exceptions, so they differ from the Unicode 5.2 table.
            case 0x0CF1: // KANNADA SIGN JIHVAMULIYA, now Lo
            case 0x0CF2: // KANNADA SIGN UPADHMANIYA, now Lo
                return true;
            case 0x19DA: // NEW TAI LUE THAM DIGIT ONE, now No
                return false;
            default:
                return !isDisallowed(c) && (!isUnassigned(c) || isDerivedPValid(c));
        }
    }

    /**
     * Classifies a code point that the bundled RFC 5892 table (Unicode 5.2) lists as unassigned, using
     * the running JDK's Unicode data and an approximation of the RFC 5892 section 3 derivation: it is
     * PVALID only if it is a lowercase, modifier or other letter, a nonspacing or spacing mark, or a
     * decimal digit (LetterDigits without Lu), and NFKC and lower-casing leave it unchanged (Unstable).
     * Symbols such as emoji, punctuation, upper case and format controls added since stay rejected.
     */
    private static boolean isDerivedPValid(int c) {
        int type = getType(c);
        // Cherokee is the one script whose case folding maps small letters to capitals, so its small
        // letters (Unicode 8) are unstable under NFKC_Casefold while its capitals stay PVALID.
        if (Character.UnicodeScript.of(c) == Character.UnicodeScript.CHEROKEE) return UPPERCASE_LETTER == type;
        // MONGOLIAN FREE VARIATION SELECTOR FOUR (Unicode 14) is a Default_Ignorable_Code_Point, so it is DISALLOWED.
        if (0x180F == c) return false;
        switch (type) {
            case LOWERCASE_LETTER:
            case MODIFIER_LETTER:
            case OTHER_LETTER:
            case NON_SPACING_MARK:
            case COMBINING_SPACING_MARK:
            case DECIMAL_DIGIT_NUMBER:
                break;
            default:
                return false;
        }
        String s = new String(Character.toChars(c));
        // Case folding approximated as lower(upper(c)): it catches letters that fold to another letter,
        // such as CYRILLIC SMALL LETTER ROUNDED VE (U+1C80), which folds to U+0432.
        return s.equals(Normalizer.normalize(s, Normalizer.Form.NFKC)) && s.equals(s.toLowerCase(Locale.ROOT))
            && c == Character.toLowerCase(Character.toUpperCase(c));
    }

    /**
     * Whenever dots are used as label separators, the following characters MUST be
     * recognized as dots: U+002E (full stop), U+3002 (ideographic full stop),
     * U+FF0E (fullwidth full stop), U+FF61 (halfwidth ideographic full stop)
     */
    private static String LABEL_SEPARATOR_REGEX = "[\\.\\u3002\\uff0e\\uff61]";

    /**
     * Validates an internationalized host name (the {@code idn-hostname} format).
     *
     * @param value the host name
     * @return whether every label is a valid U-label or A-label and the name is at most 253
     * characters in A-label form
     */
    public static boolean isValid(String value) {
        if ("".equals(value)) {
            return false; // empty string should fail
        }
        // RFC 5892 calls each segment in a host name a label. They are separated by all the recognized label separators.
        String[] labels = value.split(LABEL_SEPARATOR_REGEX);
        if (labels.length == 0) {
            return false; // a value made up only of label separators (e.g. "." or "...") has no labels
        }
        int asciiLength = labels.length - 1; // the separators
        String[] uLabels = new String[labels.length];
        for (int n = 0; n < labels.length; n++) {
            String label = labels[n];
            // String.split() drops trailing empty strings, so a trailing label
            // separator never produces an empty label here. A leading or interior
            // empty label (e.g. ".example" or "a..b") is invalid.
            if (label.isEmpty()) return false;

            String unicode;
            int aceLength;
            if (isACE(label)) {
                aceLength = label.length();
                if (MAX_LABEL_LENGTH < aceLength) return false; // checked before the quadratic decode
                unicode = toUnicode(label);
                if (null == unicode) return false;
            } else {
                // An A-label is at least as long as its U-label has code points.
                if (MAX_LABEL_LENGTH < label.codePointCount(0, label.length())) return false;
                unicode = label;
                aceLength = isAscii(label) ? label.length() : ACE_PREFIX_LENGTH + Punycode.encode(label).length();
            }
            // RFC 5891 4.2.4.  Registration Validation Requirements
            if (MAX_LABEL_LENGTH < aceLength) return false;
            asciiLength += aceLength;
            if (MAX_NAME_LENGTH < asciiLength) return false;

            if (!isValidULabel(unicode)) return false;
            uLabels[n] = unicode;
        }
        return isValidBidiDomainName(uLabels);
    }

    /**
     * Applies the RFC 5893 Bidi Rule across a whole name: once any label contains an R, AL or AN
     * character, every label of the name, ASCII ones included, must satisfy the rule.
     *
     * @param labels the labels of the name, A-labels already validated, or U-labels
     * @return whether the name is not a Bidi domain name or all of its labels satisfy the Bidi Rule
     */
    public static boolean satisfiesBidiDomainRule(String[] labels) {
        String[] uLabels = new String[labels.length];
        for (int n = 0; n < labels.length; n++) {
            uLabels[n] = isACE(labels[n]) ? toUnicode(labels[n]) : labels[n];
            if (null == uLabels[n]) return false;
        }
        return isValidBidiDomainName(uLabels);
    }

    private static boolean isValidBidiDomainName(String[] uLabels) {
        boolean bidiDomainName = false;
        for (String uLabel : uLabels) bidiDomainName |= hasRightToLeft(uLabel);
        if (!bidiDomainName) return true;
        for (String uLabel : uLabels) {
            if (!satisfiesBidiRule(uLabel)) return false;
        }
        return true;
    }

    /**
     * Validates an A-label in a {@code hostname}: it must be valid Punycode that encodes back to
     * itself, at most 63 characters, and its U-label must pass the RFC 5891 hyphen, combining mark,
     * contextual, bidi and disallowed-character rules.
     *
     * @param label the label, including the ACE prefix
     * @return whether the label is a valid A-label
     */
    public static boolean isValidALabel(String label) {
        if (MAX_LABEL_LENGTH < label.length() || !isACE(label)) return false;
        String unicode = toUnicode(label);
        return null != unicode && isValidULabel(unicode);
    }

    private static boolean isValidULabel(String unicode) {
        int len = unicode.length();
        BiPredicate<String, Integer> rules;

        // RFC 5891 5.4.  Validation and Character List Testing
        if (!Normalizer.isNormalized(unicode, Normalizer.Form.NFC)) return false;

        // RFC 5891 4.2.3.1.  Hyphen Restrictions, by code point position
        if ('-' == unicode.charAt(0) || '-' == unicode.codePointBefore(len)) return false;
        if (4 <= len) {
            int third = unicode.offsetByCodePoints(0, 2);
            if (third + 1 < len && '-' == unicode.charAt(third) && '-' == unicode.charAt(third + 1)) return false;
        }

        // RFC 5891 4.2.3.2.  Leading Combining Marks
        if (isCombiningMark(unicode.codePointAt(0))) return false;

        // RFC 5893 2.  The Bidi Rule
        switch (getDirectionality(unicode.codePointAt(0))) {
            case DIRECTIONALITY_LEFT_TO_RIGHT:
                rules = IDNA_RULES.and(LTR);
                break;
            case DIRECTIONALITY_RIGHT_TO_LEFT:
            case DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
                rules = IDNA_RULES.and(RTL);
                break;
            case DIRECTIONALITY_EUROPEAN_NUMBER:
            case DIRECTIONALITY_OTHER_NEUTRALS:
                rules = IDNA_RULES;
                break;
            default: return false;
        }

        // Step by code point, so a supplementary character is tested once and never as a lone low surrogate.
        for (int i = 0; i < len; i += Character.charCount(unicode.codePointAt(i))) {
            if (!rules.test(unicode, i)) return false;
        }

        // RFC 5893 2.  A label with an R, AL or AN character is in a Bidi domain name, so the full Bidi Rule applies.
        return !hasRightToLeft(unicode) || satisfiesBidiRule(unicode);
    }

    private static boolean hasRightToLeft(String label) {
        for (int i = 0; i < label.length(); i += Character.charCount(label.codePointAt(i))) {
            switch (getDirectionality(label.codePointAt(i))) {
                case DIRECTIONALITY_RIGHT_TO_LEFT:
                case DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
                case DIRECTIONALITY_ARABIC_NUMBER:
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * RFC 5893 2, rules 1, 3, 4 and 6. Rules 2 and 5, the allowed character classes, are checked by the
     * LTR and RTL predicates.
     */
    private static boolean satisfiesBidiRule(String label) {
        byte first = getDirectionality(label.codePointAt(0));
        boolean rtl = first == DIRECTIONALITY_RIGHT_TO_LEFT || first == DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
        // Rule 1: the first character must have the Bidi property L, R or AL.
        if (!rtl && first != DIRECTIONALITY_LEFT_TO_RIGHT) return false;

        boolean europeanNumber = false;
        boolean arabicNumber = false;
        byte last = first;
        for (int i = 0; i < label.length(); i += Character.charCount(label.codePointAt(i))) {
            byte d = getDirectionality(label.codePointAt(i));
            if (d == DIRECTIONALITY_EUROPEAN_NUMBER) europeanNumber = true;
            if (d == DIRECTIONALITY_ARABIC_NUMBER) arabicNumber = true;
            if (d != DIRECTIONALITY_NONSPACING_MARK) last = d;
        }
        if (rtl) {
            // Rule 4: an RTL label must not contain both EN and AN.
            if (europeanNumber && arabicNumber) return false;
            // Rule 3: an RTL label must end with R, AL, EN or AN, followed by zero or more NSM.
            return last == DIRECTIONALITY_RIGHT_TO_LEFT || last == DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
                || last == DIRECTIONALITY_EUROPEAN_NUMBER || last == DIRECTIONALITY_ARABIC_NUMBER;
        }
        // Rule 6: an LTR label must end with L or EN, followed by zero or more NSM.
        return last == DIRECTIONALITY_LEFT_TO_RIGHT || last == DIRECTIONALITY_EUROPEAN_NUMBER;
    }

    /**
     * Converts an A-label to its U-label (RFC 5890 2.3.2.1). The label is lower-cased first, since
     * the ACE prefix and the basic code points of an A-label are case-insensitive.
     *
     * @param label the A-label, including the ACE prefix
     * @return the U-label, or {@code null} if the label is not a valid A-label: the Punycode
     * does not decode, does not encode back to the same label, or decodes to ASCII only
     */
    private static String toUnicode(String label) {
        if (!isAscii(label)) return null;
        String punycode = label.substring(ACE_PREFIX_LENGTH).toLowerCase(Locale.ROOT);
        String unicode = Punycode.decode(punycode);
        if (null == unicode || unicode.isEmpty() || isAscii(unicode)) return null;
        if (!Punycode.encode(unicode).equals(punycode)) return null;
        return unicode;
    }

    private static boolean isAscii(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) >= 0x80) return false;
        }
        return true;
    }

    private static boolean isACE(String value) {
        return ACE_PREFIX_LENGTH <= value.length() &&
            ACE_PREFIX.equalsIgnoreCase(value.substring(0, ACE_PREFIX_LENGTH));
    }

    private static boolean isCombiningMark(int codepoint) {
        switch (getType(codepoint)) {
            case NON_SPACING_MARK:
            case ENCLOSING_MARK:
            case COMBINING_SPACING_MARK:
                return true;
            default:
                return false;
        }
    }

    /* RFC 5893 1.4 Terminology
     *  L - Left to right - most letters in LTR scripts
     *  R - Right to left - most letters in non-Arabic RTL scripts
     *  AL - Arabic letters - most letters in the Arabic script
     *  EN - European Number (0-9, and Extended Arabic-Indic numbers)
     *  ES - European Number Separator (+ and -)
     *  ET - European Number Terminator (currency symbols, the hash sign, the percent sign and so on)
     *  AN - Arabic Number; this encompasses the Arabic-Indic numbers, but not the Extended Arabic-Indic numbers
     *  CS - Common Number Separator (. , / : et al)
     *  NSM - Nonspacing Mark - most combining accents
     *  BN - Boundary Neutral - control characters (ZWNJ, ZWJ, and others)
     *  B - Paragraph Separator
     *  S - Segment Separator
     *  WS - Whitespace, including the SPACE character
     *  ON - Other Neutrals, including @, &, parentheses, MIDDLE DOT
     *  LRE, LRO, RLE, RLO, PDF - these are "directional control characters" and are not used in IDNA labels.
     */

    // RFC 5891 4.2.3.4.  Labels Containing Characters Written Right to Left
    private static boolean testLTR(String s, int i) {
        int c = s.codePointAt(i);
        switch (getDirectionality(c)) {
            case DIRECTIONALITY_LEFT_TO_RIGHT:
            case DIRECTIONALITY_EUROPEAN_NUMBER:
            case DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
            case DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
            case DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR:
            case DIRECTIONALITY_OTHER_NEUTRALS:
            case DIRECTIONALITY_BOUNDARY_NEUTRAL:
            case DIRECTIONALITY_NONSPACING_MARK:
                return true;
            default: return false;
        }
    }

    // RFC 5891 4.2.3.4.  Labels Containing Characters Written Right to Left
    private static boolean testRTL(String s, int i) {
        int c = s.codePointAt(i);
        switch (getDirectionality(c)) {
            case DIRECTIONALITY_RIGHT_TO_LEFT:
            case DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
            case DIRECTIONALITY_ARABIC_NUMBER:
            case DIRECTIONALITY_EUROPEAN_NUMBER:
            case DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR:
            case DIRECTIONALITY_COMMON_NUMBER_SEPARATOR:
            case DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR:
            case DIRECTIONALITY_OTHER_NEUTRALS:
            case DIRECTIONALITY_BOUNDARY_NEUTRAL:
            case DIRECTIONALITY_NONSPACING_MARK:
                return true;
            default: return false;
        }
    }

    /**
     * Determines whether the GREEK LOWER NUMERAL SIGN (KERAIA) conforms to the RFC 5892 specification.
     * 
     * @param s Must be a simple Unicode string; i.e., not ACE encoded 
     * @param i the location of the KERAIA within the source label
     * @return {@code true} if the KERAIA rule is valid at the given location
     *         or the character at the given position is not the KERAIA character.
     */
    private static boolean testGreekLowerNumeralSign(String s, int i) {
        int c = s.codePointAt(i);
        if (GREEK_LOWER_NUMERAL_SIGN == c) {
            // There must be a Greek character after this symbol
            if (s.length() == 1 + i) return false;
            return UnicodeScript.GREEK == UnicodeScript.of(s.codePointAt(i + 1));
        }
        return true;
    }

    /**
     * Determines whether the HEBREW PUNCTUATION (GERESH or GERSHAYIM) conforms to the RFC 5892 specification.
     * 
     * @param s Must be a simple Unicode string; i.e., not ACE encoded 
     * @param i the location of the character within the source label
     * @return {@code true} if the rule is valid at the given location
     *         or the character at the given position is not a GERESH or GERSHAYIM character.
     */
    private static boolean testHebrewPuncuation(String s, int i) {
        int c = s.codePointAt(i);
        if (HEBREW_GERESH == c || HEBREW_GERSHAYIM == c) {
            // There must be a Hebrew character before this symbol
            if (0 == i) return false;
            return UnicodeScript.HEBREW == UnicodeScript.of(s.codePointBefore(i));
        }
        return true;
    }

    /**
     * Determines whether the KATAKANA MIDDLE DOT conforms to the RFC 5892 specification.
     * 
     * @param s Must be a simple Unicode string; i.e., not ACE encoded 
     * @param i the location of the character within the source label
     * @return {@code true} if the rule is valid at the given location
     *         or the character at the given position is not a KATAKANA MIDDLE DOT character.
     */
    private static boolean testKatakanaMiddleDot(String s, int i) {
        int c = s.codePointAt(i);
        if (KATAKANA_MIDDLE_DOT == c) {
            // The label must contain a Hiragana, Katakana or Han character anywhere, not just after this symbol
            return s.codePoints().anyMatch(cp -> {
                if (KATAKANA_MIDDLE_DOT == cp) return false;
                UnicodeScript script = UnicodeScript.of(cp);
                return UnicodeScript.HIRAGANA == script || UnicodeScript.KATAKANA == script || UnicodeScript.HAN == script;
            });
        }
        return true;
    }

    /**
     * Determines whether the MIDDLE DOT conforms to the RFC 5892 specification.
     * 
     * @param s Must be a simple Unicode string; i.e., not ACE encoded 
     * @param i the location of the MIDDLE DOT within the source label
     * @return {@code true} if the MIDDLE DOT rule is valid at the given location
     *         or the character at the given position is not the MIDDLE DOT character.
     */
    private static boolean testeMiddleDotRule(String s, int i) {
        int c = s.codePointAt(i);
        if (MIDDLE_DOT == c) {
            // There must be a 'l' character before and after this symbol
            if (0 == i) return false;
            if (s.length() == 1 + i) return false;
            return 'l' == s.codePointBefore(i) && 'l' == s.codePointAt(i + 1);
        }
        return true;
    }

    private static boolean isVirama(int codepoint) {
        return Arrays.binarySearch(VIRAMAS, codepoint) >= 0;
    }

    /**
     * Determines whether the ZERO WIDTH JOINER conforms to the RFC 5892 specification.
     * 
     * @param s Must be a simple Unicode string; i.e., not ACE encoded 
     * @param i the location of the character within the source label
     * @return {@code true} if the rule is valid at the given location
     *         or the character at the given position is not a ZERO WIDTH JOINER character.
     */
    private static boolean testZeroWidthJoiner(String s, int i) {
        int c = s.codePointAt(i);
        if (ZERO_WIDTH_JOINER == c) {
            // There must be a virama character before this symbol.
            if (0 == i) return false;
            return isVirama(s.codePointBefore(i));
        }
        return true;
    }

    /**
     * Determines whether the ZERO WIDTH NON-OINER conforms to the RFC 5892 specification.
     * 
     * @param s Must be a simple Unicode string; i.e., not ACE encoded 
     * @param i the location of the character within the source label
     * @return {@code true} if the rule is valid at the given location
     *         or the character at the given position is not a ZERO WIDTH NON-JOINER character.
     */
    private static boolean testZeroWidthNonJoiner(String s, int i) {
        int c = s.codePointAt(i);
        if (ZERO_WIDTH_NON_JOINER == c) {
            // There must be a virama character before this symbol or
            // If RegExpMatch((Joining_Type:{L,D})(Joining_Type:T)*\u200C(Joining_Type:T)*(Joining_Type:{R,D})) Then True;

            if (0 == i) return false;
            int preceding = s.codePointBefore(i);
            if (isVirama(preceding)) return true;

            // Step by code point, as several scripts have supplementary transparent marks.
            int j = i;
            while (0 < j && isJoinTypeTransparent(s.codePointBefore(j))) j -= Character.charCount(s.codePointBefore(j));
            if (0 == j) return false;

            preceding = s.codePointBefore(j);
            if (!isJoinTypeLeft(preceding) && !isJoinTypeDual(preceding)) return false;

            j = i + 1;
            int len = s.length();
            if (len == j) return false;

            while (j < len && isJoinTypeTransparent(s.codePointAt(j))) j += Character.charCount(s.codePointAt(j));
            if (len == j) return false;

            int following = s.codePointAt(j);
	        return isJoinTypeRight(following) || isJoinTypeDual(following);
        }
        return true;
    }

    private static boolean testArabicIndicDigit(String s, int i) {
        int c = s.codePointAt(i);
        if (isArabicIndicDigit(c)) {
            return !s.codePoints().anyMatch(UnicodeDatabase::isExtendedArabicIndicDigit);
        }
        return true;
    }

    private static boolean testExtendedArabicIndicDigit(String s, int i) {
        int c = s.codePointAt(i);
        if (isExtendedArabicIndicDigit(c)) {
            return !s.codePoints().anyMatch(UnicodeDatabase::isArabicIndicDigit);
        }
        return true;
    }

    private static synchronized void loadDerivedProperties() {
        if (!loaded) {
            UCDLoader.loadMapping("/ucd/RFC5892-appendix-B.txt", v -> {
                switch (v) {
                    case "CONTEXTJ": return CONTEXTJ;
                    case "CONTEXTO": return CONTEXTO;
                    case "DISALLOWED": return DISALLOWED;
                    case "UNASSIGNED": return UNASSIGNED;
                    default: return null;
                }
            });

            // We have IDNA rules for these.
            CONTEXTJ.clear(ZERO_WIDTH_JOINER);
            CONTEXTJ.clear(ZERO_WIDTH_NON_JOINER);
            CONTEXTO.clear(0x660, 0x066A); // ARABIC-INDIC DIGITS
            CONTEXTO.clear(0x6F0, 0x06FA); // EXTENDED ARABIC-INDIC DIGITS
            CONTEXTO.clear(GREEK_LOWER_NUMERAL_SIGN);
            CONTEXTO.clear(HEBREW_GERESH);
            CONTEXTO.clear(HEBREW_GERSHAYIM);
            CONTEXTO.clear(KATAKANA_MIDDLE_DOT);
            CONTEXTO.clear(MIDDLE_DOT);
            // The volatile write publishes the filled tables to threads that read loaded without the lock.
            loaded = true;
        }
    }

}
