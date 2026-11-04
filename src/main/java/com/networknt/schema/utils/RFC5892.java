package com.networknt.schema.utils;

import java.text.Normalizer;
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
    private static final int VIRAMA = 0x94D;
    private static final int ZERO_WIDTH_JOINER = 0x200D;
    private static final int ZERO_WIDTH_NON_JOINER = 0x200C;

    private static final BitSet CONTEXTJ = new BitSet(0x110000);
    private static final BitSet CONTEXTO = new BitSet(0x110000);
    private static final BitSet DISALLOWED = new BitSet(0x110000);
    private static final BitSet UNASSIGNED = new BitSet(0x110000);

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

    private static final BiPredicate<String, Integer> IDNA_RULES = idnaRules(ALLOWED_CHARACTER);

    /**
     * The IDNA rules, except that a code point unassigned in the bundled RFC 5892 table is not rejected
     * when the running JDK's Unicode version assigns it. The table is Unicode 5.2, so it would reject
     * every code point added since.
     */
    private static final BiPredicate<String, Integer> IDNA_RULES_ALLOWING_UNASSIGNED =
        idnaRules(RFC5892::testAllowedOrUnassignedCharacter);

    private static BiPredicate<String, Integer> idnaRules(BiPredicate<String, Integer> allowedCharacter) {
        return allowedCharacter
            .and(RULE_ARABIC_INDIC_DIGITS_RULE)
            .and(RULE_EXTENDED_ARABIC_INDIC_DIGITS_RULE)
            .and(RULE_GREEK_LOWER_NUMERAL_SIGN)
            .and(RULE_HEBREW_GERESH_GERSHAYIM)
            .and(RULE_KATAKANA_MIDDLE_DOT)
            .and(RULE_MIDDLE_DOT)
            .and(RULE_ZERO_WIDTH_JOINER)
            .and(RULE_ZERO_WIDTH_NON_JOINER);
    }

    private static boolean isContextJ(int codepoint) {
        if (CONTEXTJ.isEmpty()) loadDerivedProperties();
        return CONTEXTJ.get(codepoint);
    }

    private static boolean isContextO(int codepoint) {
        if (CONTEXTO.isEmpty()) loadDerivedProperties();
        return CONTEXTO.get(codepoint);
    }

    private static boolean isDisallowed(int codepoint) {
        if (DISALLOWED.isEmpty()) loadDerivedProperties();
        return DISALLOWED.get(codepoint);
    }

    private static boolean isUnassigned(int codepoint) {
        if (UNASSIGNED.isEmpty()) loadDerivedProperties();
        return UNASSIGNED.get(codepoint);
    }

    private static boolean testAllowedCharacter(String s, int i) {
        int c = s.codePointAt(i);
        return !isDisallowed(c) && !isUnassigned(c) // RFC 5891 4.2.2.  Rejection of Characters That Are Not Permitted
            && !isContextJ(c)   && !isContextO(c);  // RFC 5891 4.2.3.3.  Contextual Rules
    }

    private static boolean testAllowedOrUnassignedCharacter(String s, int i) {
        int c = s.codePointAt(i);
        return !isDisallowed(c) && (!isUnassigned(c) || Character.isDefined(c))
            && !isContextJ(c) && !isContextO(c);
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
        for (String label : labels) {
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

            if (!isValidULabel(unicode, IDNA_RULES)) return false;
        }

        return true;
    }

    /**
     * Validates an A-label in a {@code hostname}: it must be valid Punycode that encodes back to
     * itself, at most 63 characters, and its U-label must pass the RFC 5891 hyphen, combining mark,
     * contextual and bidi rules. A code point that the bundled RFC 5892 table lists as unassigned is
     * not rejected when the JDK assigns it, so names that use code points added in later Unicode
     * versions stay valid.
     *
     * @param label the label, including the ACE prefix
     * @return whether the label is a valid A-label
     */
    public static boolean isValidALabel(String label) {
        if (MAX_LABEL_LENGTH < label.length() || !isACE(label)) return false;
        String unicode = toUnicode(label);
        return null != unicode && isValidULabel(unicode, IDNA_RULES_ALLOWING_UNASSIGNED);
    }

    private static boolean isValidULabel(String unicode, BiPredicate<String, Integer> idnaRules) {
        int len = unicode.length();
        BiPredicate<String, Integer> rules;

        // RFC 5891 5.4.  Validation and Character List Testing
        if (!Normalizer.isNormalized(unicode, Normalizer.Form.NFC)) return false;

        // RFC 5891 4.2.3.1.  Hyphen Restrictions
        if ('-' == unicode.charAt(0) || '-' == unicode.codePointBefore(len)) return false;
        if (4 <= len && '-' == unicode.codePointAt(2) && '-' == unicode.codePointAt(3)) return false;

        // RFC 5891 4.2.3.2.  Leading Combining Marks
        if (isCombiningMark(unicode.codePointAt(0))) return false;

        // RFC 5893 2.  The Bidi Rule
        switch (getDirectionality(unicode.codePointAt(0))) {
            case DIRECTIONALITY_LEFT_TO_RIGHT:
                rules = idnaRules.and(LTR);
                break;
            case DIRECTIONALITY_RIGHT_TO_LEFT:
            case DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
                rules = idnaRules.and(RTL);
                break;
            case DIRECTIONALITY_EUROPEAN_NUMBER:
            case DIRECTIONALITY_OTHER_NEUTRALS:
                rules = idnaRules;
                break;
            default: return false;
        }

        // Step by code point, so a supplementary character is tested once and never as a lone low surrogate.
        for (int i = 0; i < len; i += Character.charCount(unicode.codePointAt(i))) {
            if (!rules.test(unicode, i)) return false;
        }
        return true;
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
        return value.chars().allMatch(c -> c < 0x80);
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
            int following = s.codePointAt(i + 1);
	        return isGreek(following);
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
            int preceding = s.codePointAt(i - 1);
	        return isHebrew(preceding);
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
            // There must be a Katakana, Hiragana or Han character after this symbol
            if (s.length() == 1 + i) return false;
            int following = s.codePointAt(i + 1);
	        return isKatakana(following);
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
            int preceding = s.codePointAt(i - 1);
            int following = s.codePointAt(i + 1);
	        return 'l' == preceding && 'l' == following;
        }
        return true;
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
            int preceding = s.codePointAt(i - 1);
	        return VIRAMA == preceding;
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
            if (VIRAMA == preceding) return true;

            int j = i;
            while (0 < j && isJoinTypeTransparent(s.codePointBefore(j))) --j;
            if (0 == j) return false;

            preceding = s.codePointBefore(j);
            if (!isJoinTypeLeft(preceding) && !isJoinTypeDual(preceding)) return false;

            j = i + 1;
            int len = s.length();
            if (len == j) return false;

            while (j < len && isJoinTypeTransparent(s.codePointAt(j))) ++j;
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
        if (DISALLOWED.isEmpty()) {
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
        }
    }

}
