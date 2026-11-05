package com.networknt.schema.utils;

import java.util.BitSet;

public class UnicodeDatabase {
    private static final BitSet ARABIC_INDIC_DIGITS = new BitSet(0x11000);
    private static final BitSet EXTENDED_ARABIC_INDIC_DIGITS = new BitSet(0x11000);

    private static final BitSet JOIN_TYPE_CAUSING = new BitSet(0x110000);
    private static final BitSet JOIN_TYPE_DUAL = new BitSet(0x110000);
    private static final BitSet JOIN_TYPE_LEFT = new BitSet(0x110000);
    private static final BitSet JOIN_TYPE_RIGHT = new BitSet(0x110000);
    private static final BitSet JOIN_TYPE_TRANSPARENT = new BitSet(0x110000);
    private static volatile boolean joiningTypesLoaded;

    static {
        // TODO: Should we initialize this lazily?
        ARABIC_INDIC_DIGITS.set(0x0660, 0x066A);
        EXTENDED_ARABIC_INDIC_DIGITS.set(0x06F0, 0x6FA);
   }

    public static boolean isArabicIndicDigit(int codepoint) {
        return ARABIC_INDIC_DIGITS.get(codepoint);
    }

    public static boolean isExtendedArabicIndicDigit(int codepoint) {
        return EXTENDED_ARABIC_INDIC_DIGITS.get(codepoint);
    }

    public static boolean isJoinTypeCausing(int codepoint) {
        if (!joiningTypesLoaded) loadJoiningTypes();
        return JOIN_TYPE_CAUSING.get(codepoint);
    }

    public static boolean isJoinTypeDual(int codepoint) {
        if (!joiningTypesLoaded) loadJoiningTypes();
        return JOIN_TYPE_DUAL.get(codepoint);
    }

    public static boolean isJoinTypeLeft(int codepoint) {
        if (!joiningTypesLoaded) loadJoiningTypes();
        return JOIN_TYPE_LEFT.get(codepoint);
    }

    public static boolean isJoinTypeRight(int codepoint) {
        if (!joiningTypesLoaded) loadJoiningTypes();
        return JOIN_TYPE_RIGHT.get(codepoint);
    }

    public static boolean isJoinTypeTransparent(int codepoint) {
        if (!joiningTypesLoaded) loadJoiningTypes();
        return JOIN_TYPE_TRANSPARENT.get(codepoint);
    }

    private static synchronized void loadJoiningTypes() {
        if (!joiningTypesLoaded) {
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
            // The volatile write publishes the filled tables to threads that read the flag without the lock.
            joiningTypesLoaded = true;
        }
    }

}
