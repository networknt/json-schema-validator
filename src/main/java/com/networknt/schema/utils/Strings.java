/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

/**
 * Utility methods for working with Strings.
 */
public final class Strings {
    private static final char CHAR_0 = '0';
    private static final char CHAR_1 = '1';
    private static final char CHAR_9 = '9';
    private static final char MINUS = '-';
    private static final char PLUS = '+';
    private static final char DOT = '.';
    private static final char CHAR_E = 'E';
    private static final char CHAR_e = 'e';

    private Strings() {
    }

    public static boolean isInteger(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }

        // all code below could be replaced with
        //return str.matrch("[-+]?(?:0|[1-9]\\d*)")
        int i = 0;
        if (string.charAt(0) == '-' || string.charAt(0) == '+') {
            if (string.length() == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isBoolean(String string) {
        return "true".equals(string) || "false".equals(string);
    }

    public static boolean isNumeric(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }

        // all code below could be replaced with
        //return str.matrch("[-+]?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")
        int i = 0;
        int len = string.length();

        if (string.charAt(i) == MINUS || string.charAt(i) == PLUS) {
            if (string.length() == 1) {
                return false;
            }
            i = 1;
        }

        char character = string.charAt(i++);

        if (character == CHAR_0) {
            // TODO: if leading zeros are supported (counter to JSON spec) handle it here
            if (i < len) {
                character = string.charAt(i++);
                if (character != DOT && character != CHAR_E && character != CHAR_e) {
                    return false;
                }
            }
        } else if (CHAR_1 <= character && character <= CHAR_9) {
            while (i < len && CHAR_0 <= character && character <= CHAR_9) {
                character = string.charAt(i++);
            }
        } else {
            return false;
        }

        if (character == DOT) {
            if (i >= len) {
                return false;
            }
            character = string.charAt(i++);
            while (i < len && CHAR_0 <= character && character <= CHAR_9) {
                character = string.charAt(i++);
            }
        }

        if (character == CHAR_E || character == CHAR_e) {
            if (i >= len) {
                return false;
            }
            character = string.charAt(i++);
            if (character == PLUS || character == MINUS) {
                if (i >= len) {
                    return false;
                }
                character = string.charAt(i++);
            }
            while (i < len && CHAR_0 <= character && character <= CHAR_9) {
                character = string.charAt(i++);
            }
        }

        return i >= len && (CHAR_0 <= character && character <= CHAR_9);
    }

    public static boolean isBlank(String string) {
        return null == string || string.trim().isEmpty();
    }
}
