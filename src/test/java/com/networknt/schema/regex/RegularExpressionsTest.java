/*
 * Copyright (c) 2024 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class RegularExpressionsTest {
    enum DollarAnchorInput {
        ANCHOR("testing$", "testing\\z"),
        COMPLEX_ANCHOR("(hello$|world$|today$)", "(hello\\z|world\\z|today\\z)"),
        CHARACTER_CLASS("[a-Z$]", "[a-Z$]"),
        QUOTED_LITERAL_SECTION("\\Q$\\E", "\\Q$\\E"),
        ESCAPED("\\$", "\\$"),
        ;

        String regex;
        String result;

        DollarAnchorInput(String regex, String result) {
            this.regex = regex;
            this.result = result;
        }
    }

    @ParameterizedTest
    @EnumSource(DollarAnchorInput.class)
    void dollarAnchor(DollarAnchorInput input) {
        String result = RegularExpressions.replaceDollarAnchors(input.regex);
        assertEquals(input.result, result);
    }

    private static final Map<String, String> CHARACTER_CLASSES;
    static {
        CHARACTER_CLASSES = new HashMap<>();
        CHARACTER_CLASSES.put("Letter", "L");
    }

    enum CharacterClassInput {
        LETTER("abc\\p{Letter}abc", "abc\\p{L}abc"),
        NO_BRACE("abc\\p{Letterabc", "abc\\p{Letterabc"),
        ;

        String regex;
        String result;

        CharacterClassInput(String regex, String result) {
            this.regex = regex;
            this.result = result;
        }
    }

    @ParameterizedTest
    @EnumSource(CharacterClassInput.class)
    void characterClass(CharacterClassInput input) {
        String result = RegularExpressions.replaceCharacterProperties(input.regex, CHARACTER_CLASSES);
        assertEquals(input.result, result);
    }
}
