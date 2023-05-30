package com.networknt.schema.utils;

import java.util.BitSet;

public class UnicodeDatabase {
    private static final BitSet ARABIC_INDIC_DIGITS = new BitSet(0x11000);
    private static final BitSet EXTENDED_ARABIC_INDIC_DIGITS = new BitSet(0x11000);
    private static final BitSet GREEK_CHARACTERS = new BitSet(0x2000);
    private static final BitSet HEBREW_CHARACTERS = new BitSet(0x0600);
    private static final BitSet KATAKANA_CHARACTERS = new BitSet(0x33000);

    private static final BitSet JOIN_TYPE_CAUSING = new BitSet(0x110000);
    private static final BitSet JOIN_TYPE_DUAL = new BitSet(0x110000);
    private static final BitSet JOIN_TYPE_LEFT = new BitSet(0x110000);
    private static final BitSet JOIN_TYPE_RIGHT = new BitSet(0x110000);
    private static final BitSet JOIN_TYPE_TRANSPARENT = new BitSet(0x110000);

    static {
        // TODO: Should we initialize this lazily?
        ARABIC_INDIC_DIGITS.set(0x0660, 0x066A);
        EXTENDED_ARABIC_INDIC_DIGITS.set(0x06F0, 0x6FA);
        GREEK_CHARACTERS.set(0x0370, 0x0400);
        GREEK_CHARACTERS.set(0x1F00, 0x2000);
        HEBREW_CHARACTERS.set(0x0590, 0x0600);
        KATAKANA_CHARACTERS.set(0x2E80, 0x2F00); // The CJK Radicals Supplement code block
        KATAKANA_CHARACTERS.set(0x2F00, 0x2FE0); // The Kangxi Radicals code block
        KATAKANA_CHARACTERS.set(0x3000, 0x3040); // The CJK Symbols and Punctuation code block
        KATAKANA_CHARACTERS.set(0x3040, 0x30A0); // The Hiragana code block.
        KATAKANA_CHARACTERS.set(0x30A0, 0x3100); // The Katakana code block.
        KATAKANA_CHARACTERS.set(0x3400, 0x4DC0); // The CJK Unified Ideographs Extension A code block
        KATAKANA_CHARACTERS.set(0x4E00, 0xA000); // The CJK Unified Ideographs code block
        KATAKANA_CHARACTERS.set(0xF900, 0xFB00); // The CJK Compatibility Ideographs code block
        KATAKANA_CHARACTERS.set(0x16FE0, 0x17000); // The Ideographic Symbols and Punctuation code block
        KATAKANA_CHARACTERS.set(0x20000, 0x2A6E0); // The CJK Unified Ideographs Extension B code block
        KATAKANA_CHARACTERS.set(0x2A700, 0x2B740); // The CJK Unified Ideographs Extension C code block
        KATAKANA_CHARACTERS.set(0x2B740, 0x2B820); // The CJK Unified Ideographs Extension D code block
        KATAKANA_CHARACTERS.set(0x2B820, 0x2CEB0); // The CJK Unified Ideographs Extension E code block
        KATAKANA_CHARACTERS.set(0x2CEB0, 0x2EBF0); // The CJK Unified Ideographs Extension F code block
        KATAKANA_CHARACTERS.set(0x2F800, 0x2FA20); // The CJK Compatibility Ideographs Supplement code block
        KATAKANA_CHARACTERS.set(0x30000, 0x31350); // The CJK Unified Ideographs Extension G code block
        KATAKANA_CHARACTERS.set(0x31350, 0x323B0); // The CJK Unified Ideographs Extension H code block
   }

    public static boolean isArabicIndicDigit(int codepoint) {
        return ARABIC_INDIC_DIGITS.get(codepoint);
    }

    public static boolean isExtendedArabicIndicDigit(int codepoint) {
        return EXTENDED_ARABIC_INDIC_DIGITS.get(codepoint);
    }

    public static boolean isGreek(int codepoint) {
        return GREEK_CHARACTERS.get(codepoint);
    }

    public static boolean isHebrew(int codepoint) {
        return HEBREW_CHARACTERS.get(codepoint);
    }

    public static boolean isKatakana(int codepoint) {
        return KATAKANA_CHARACTERS.get(codepoint);
    }

    public static boolean isJoinTypeCausing(int codepoint) {
        if (JOIN_TYPE_CAUSING.isEmpty()) loadJoiningTypes();
        return JOIN_TYPE_CAUSING.get(codepoint);
    }

    public static boolean isJoinTypeDual(int codepoint) {
        if (JOIN_TYPE_DUAL.isEmpty()) loadJoiningTypes();
        return JOIN_TYPE_DUAL.get(codepoint);
    }

    public static boolean isJoinTypeLeft(int codepoint) {
        if (JOIN_TYPE_LEFT.isEmpty()) loadJoiningTypes();
        return JOIN_TYPE_LEFT.get(codepoint);
    }

    public static boolean isJoinTypeRight(int codepoint) {
        if (JOIN_TYPE_RIGHT.isEmpty()) loadJoiningTypes();
        return JOIN_TYPE_RIGHT.get(codepoint);
    }

    public static boolean isJoinTypeTransparent(int codepoint) {
        if (JOIN_TYPE_TRANSPARENT.isEmpty()) loadJoiningTypes();
        return JOIN_TYPE_TRANSPARENT.get(codepoint);
    }

    private static synchronized void loadJoiningTypes() {
        if (JOIN_TYPE_DUAL.isEmpty()) {
            UCDLoader.loadMapping("/ucd/extracted/DerivedJoiningType.txt", v -> {
                switch (v) {
                    case "C": return JOIN_TYPE_CAUSING;
                    case "D": return JOIN_TYPE_DUAL;
                    case "L": return JOIN_TYPE_LEFT;
                    case "R": return JOIN_TYPE_RIGHT;
                    case "T": return JOIN_TYPE_TRANSPARENT;
                    default: return null;
                }
            });
        }
    }

}
