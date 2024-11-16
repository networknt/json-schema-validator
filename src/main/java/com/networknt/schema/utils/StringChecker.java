package com.networknt.schema.utils;

public class StringChecker {

    private static final char CHAR_0 = '0';
    private static final char CHAR_1 = '1';
    private static final char CHAR_9 = '9';
    private static final char MINUS = '-';
    private static final char PLUS = '+';
    private static final char DOT = '.';
    private static final char CHAR_E = 'E';
    private static final char CHAR_e = 'e';

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // all code below could be replaced with
        //return str.matrch("[-+]?(?:0|[1-9]\\d*)")
        int i = 0;
        if (str.charAt(0) == '-' || str.charAt(0) == '+') {
            if (str.length() == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isBoolean(String s) {
        return "true".equals(s) || "false".equals(s);
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // all code below could be replaced with
        //return str.matrch("[-+]?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")
        int i = 0;
        int len = str.length();

        if (str.charAt(i) == MINUS || str.charAt(i) == PLUS) {
            if (str.length() == 1) {
                return false;
            }
            i = 1;
        }

        char character = str.charAt(i++);

        if (character == CHAR_0) {
            // TODO: if leading zeros are supported (counter to JSON spec) handle it here
            if (i < len) {
                character = str.charAt(i++);
                if (character != DOT && character != CHAR_E && character != CHAR_e) {
                    return false;
                }
            }
        } else if (CHAR_1 <= character && character <= CHAR_9) {
            while (i < len && CHAR_0 <= character && character <= CHAR_9) {
                character = str.charAt(i++);
            }
        } else {
            return false;
        }

        if (character == DOT) {
            if (i >= len) {
                return false;
            }
            character = str.charAt(i++);
            while (i < len && CHAR_0 <= character && character <= CHAR_9) {
                character = str.charAt(i++);
            }
        }

        if (character == CHAR_E || character == CHAR_e) {
            if (i >= len) {
                return false;
            }
            character = str.charAt(i++);
            if (character == PLUS || character == MINUS) {
                if (i >= len) {
                    return false;
                }
                character = str.charAt(i++);
            }
            while (i < len && CHAR_0 <= character && character <= CHAR_9) {
                character = str.charAt(i++);
            }
        }

        return i >= len && (CHAR_0 <= character && character <= CHAR_9);
    }
}
