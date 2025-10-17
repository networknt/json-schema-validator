/*
 * Copyright (c) 2025 the original author or authors.
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
package com.networknt.schema.regex;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for Regular Expressions.
 */
public class RegularExpressions {
    private RegularExpressions() {
    }

    /**
     * The meaning of $ in ecmascript does not allow newlines while for other
     * languages it is typically allowed. The closest to the meaning in ecmascript
     * is \z.
     * 
     * @param regex the regex
     * @return the replacement
     */
    public static String replaceDollarAnchors(String regex) {
        if (regex.indexOf('$') == -1) {
            return regex;
        }
        /*
         * Note that for joni there's no option for this and this occurs in the Lexer
         * when the regex is compiled. If single line $ is AnchorType.SEMI_END_BUF and
         * if multiline is AnchorType.END_LINE. However what is required is
         * AnchorType.END_BUF.
         */
        StringBuilder result = new StringBuilder();
        boolean inCharacterClass = false;
        boolean inLiteralSection = false; // This isn't supported by ECMA but by Java
        for (int i = 0; i < regex.length(); i++) {
            char ch = regex.charAt(i);
            // Literal Section (not supported by ECMA)
            if (inLiteralSection) {
                if (ch == '\\' && i + 1 < regex.length() && regex.charAt(i + 1) == 'E') {
                    result.append("\\E");
                    inLiteralSection = false;
                    i++;
                } else {
                    // Everything else is treated as a literal character
                    result.append(ch);
                }
                continue;
            }
            // Escaped
            if (ch == '\\') {
                result.append(ch);
                if (i + 1 < regex.length()) {
                    char escapedChar = regex.charAt(i + 1); 
                    result.append(escapedChar);
                    if (escapedChar == 'Q') {
                        inLiteralSection = true;
                    }
                    i++;
                }
                continue;
            }
            // Character Class
            if (ch == '[') {
                inCharacterClass = true;
                result.append(ch);
                continue;
            } else if (ch == ']') {
                inCharacterClass = false;
                result.append(ch);
                continue;
            }

            if (ch == '$') {
                if (inCharacterClass) {
                    result.append(ch);
                } else {
                    result.append("\\z");
                }
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
    
    private static final Map<String, String> LONGFORM_CHARACTER_PROPERTIES;
    
    static {
        LONGFORM_CHARACTER_PROPERTIES = new HashMap<>();
        LONGFORM_CHARACTER_PROPERTIES.put("Letter", "L");
        LONGFORM_CHARACTER_PROPERTIES.put("Lowercase_Letter", "Ll");
        LONGFORM_CHARACTER_PROPERTIES.put("Uppercase_Letter", "Lu");
        LONGFORM_CHARACTER_PROPERTIES.put("Titlecase_Letter", "Lt");
        LONGFORM_CHARACTER_PROPERTIES.put("Cased_Letter", "L&");
        LONGFORM_CHARACTER_PROPERTIES.put("Modifier_Letter", "Lm");
        LONGFORM_CHARACTER_PROPERTIES.put("Other_Letter", "Lo");
        LONGFORM_CHARACTER_PROPERTIES.put("Mark", "M");
        LONGFORM_CHARACTER_PROPERTIES.put("Non_Spacing_Mark", "Mn");
        LONGFORM_CHARACTER_PROPERTIES.put("Spacing_Combining_Mark", "Mc");
        LONGFORM_CHARACTER_PROPERTIES.put("Enclosing_Mark", "Me");
        LONGFORM_CHARACTER_PROPERTIES.put("Separator", "Z");
        LONGFORM_CHARACTER_PROPERTIES.put("Space_Separator", "Zs");
        LONGFORM_CHARACTER_PROPERTIES.put("Line_Separator", "Zl");
        LONGFORM_CHARACTER_PROPERTIES.put("Paragraph_Separator", "Zp");
        LONGFORM_CHARACTER_PROPERTIES.put("Symbol", "S");
        LONGFORM_CHARACTER_PROPERTIES.put("Math_Symbol", "Sm");
        LONGFORM_CHARACTER_PROPERTIES.put("Currency_Symbol", "Sc");
        LONGFORM_CHARACTER_PROPERTIES.put("Modifier_Symbol", "Sk");
        LONGFORM_CHARACTER_PROPERTIES.put("Other_Symbol", "So");
        LONGFORM_CHARACTER_PROPERTIES.put("Number", "N");
        LONGFORM_CHARACTER_PROPERTIES.put("Decimal_Digit_Number", "Nd");
        LONGFORM_CHARACTER_PROPERTIES.put("Letter_Number", "Nl");
        LONGFORM_CHARACTER_PROPERTIES.put("Other_Number", "No");
        LONGFORM_CHARACTER_PROPERTIES.put("Punctuation", "P");
        LONGFORM_CHARACTER_PROPERTIES.put("Dash_Punctuation", "Pd");
        LONGFORM_CHARACTER_PROPERTIES.put("Open_Punctuation", "Ps");
        LONGFORM_CHARACTER_PROPERTIES.put("Close_Punctuation", "Pe");
        LONGFORM_CHARACTER_PROPERTIES.put("Initial_Punctuation", "Pi");
        LONGFORM_CHARACTER_PROPERTIES.put("Final_Punctuation", "Pf");
        LONGFORM_CHARACTER_PROPERTIES.put("Connector_Punctuation", "Pc");
        LONGFORM_CHARACTER_PROPERTIES.put("Other_Punctuation", "Po");
        LONGFORM_CHARACTER_PROPERTIES.put("Other", "C");
        LONGFORM_CHARACTER_PROPERTIES.put("Control", "Cc");
        LONGFORM_CHARACTER_PROPERTIES.put("Format", "Cf");
        LONGFORM_CHARACTER_PROPERTIES.put("Private_Use", "Co");
        LONGFORM_CHARACTER_PROPERTIES.put("Surrogate", "Cs");
        LONGFORM_CHARACTER_PROPERTIES.put("Unassigned", "Cn");
        LONGFORM_CHARACTER_PROPERTIES.put("digit", "Nd");
    }

    /**
     * Replaces the longform character properties with the shortform character
     * propertise.
     * 
     * @param regex the regex
     * @return the replacement
     */
    public static String replaceLongformCharacterProperties(String regex) {
        return replaceCharacterProperties(regex, LONGFORM_CHARACTER_PROPERTIES);
    }

    /**
     * The character properties in JDK is different from ECMA.
     * 
     * @param regex the regex
     * @return the replacement
     */
    public static String replaceCharacterProperties(String regex, Map<String, String> replacements) {
        if (regex.indexOf("\\p{") == -1) {
            return regex;
        }
        StringBuilder result = new StringBuilder();
        boolean inCharacterClass = false;
        boolean inLiteralSection = false; // This isn't supported by ECMA but by Java
        for (int i = 0; i < regex.length(); i++) {
            char ch = regex.charAt(i);
            // Literal Section (not supported by ECMA)
            if (inLiteralSection) {
                if (ch == '\\' && i + 1 < regex.length() && regex.charAt(i + 1) == 'E') {
                    result.append("\\E");
                    inLiteralSection = false;
                    i++;
                } else {
                    // Everything else is treated as a literal character
                    result.append(ch);
                }
                continue;
            }
            if (!inCharacterClass && regex.length() >= i + 3 && regex.startsWith("\\p{", i)) {
                
                // Find the matching closing brace '}'
                int end = findClosingBrace(regex, i + 3);

                if (end != -1) {
                    // Found valid \p{...} outside character class and literal block
                    result.append("\\p{");
                    String characterClass = regex.substring(i + 3, end);
                    String replacement = replacements.get(characterClass);
                    if (replacement == null) {
                        result.append(characterClass);
                    } else {
                        result.append(replacement);
                    }
                    result.append("}");
                    i = end; // Skip the entire \p{...} sequence
                    continue;
                }
                // If the closing brace isn't found, fall through and treat as literals
            }            
            // Escaped
            if (ch == '\\') {
                result.append(ch);
                if (i + 1 < regex.length()) {
                    char escapedChar = regex.charAt(i + 1); 
                    result.append(escapedChar);
                    if (escapedChar == 'Q') {
                        inLiteralSection = true;
                    }
                    i++;
                }
                continue;
            }
            // Character Class
            if (ch == '[') {
                inCharacterClass = true;
                result.append(ch);
                continue;
            } else if (ch == ']') {
                inCharacterClass = false;
                result.append(ch);
                continue;
            }
            result.append(ch);
        }
        return result.toString();
    }

    private static int findClosingBrace(String regex, int start) {
        int i = start;
        while (i < regex.length()) {
            if (regex.charAt(i) == '}') {
                return i;
            }
            if (regex.charAt(i) == '\\' && i + 1 < regex.length()) {
                i++;
            }
            i++;
        }
        return -1;
    }
}
