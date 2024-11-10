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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joni.exception.SyntaxException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests for JoniRegularExpression.
 */
class JoniRegularExpressionTest {

    enum InvalidEscapeInput {
        A("\\a"),
        HELLOA("hello\\a"),
        C("\\c"),
        E("\\e"),
        G("\\g"),
        H("\\h"),
        I("\\i"),
        J("\\j"),
        K("\\k"),
        L("\\l"),
        M("\\m"),
        O("\\o"),
        Q("\\q"),
        U("\\u"),
        X("\\x"),
        X1("\\x1"),
        XGG("\\xgg"),
        X1G("\\x1g"),
        Y("\\y"),
        Z("\\z"),
        _1("\\1"),
        _2("\\2"),
        _3("\\3"),
        _4("\\4"),
        _5("\\5"),
        _6("\\6"),
        _7("\\7"),
        _8("\\8"),
        _9("\\9");

        String value;

        InvalidEscapeInput(String value) {
            this.value = value;
        }
    }

    @ParameterizedTest
    @EnumSource(InvalidEscapeInput.class)
    void invalidEscape(InvalidEscapeInput input) {
        SyntaxException e = assertThrows(SyntaxException.class, () -> new JoniRegularExpression(input.value));
        assertEquals("Invalid escape", e.getMessage());
    }

    enum ValidEscapeInput {
        B("\\b"),
        D("\\d"),
        CAP_D("\\D"),
        W("\\w"),
        CAP_W("\\W"),
        S("\\s"),
        CAP_S("\\S"),
        T("\\t"),
        U1234("\\u1234"),
        R("\\r"),
        N("\\n"),
        V("\\v"),
        F("\\f"),
        X12("\\x12"),
        X1F("\\x1f"),
        X1234("\\x1234"),
        P("\\p{Letter}cole"), // unicode property
        CAP_P("\\P{Letter}cole"), // unicode property
        _0("\\0"),
        CA("\\cA"), // control
        CB("\\cB"), // control
        CC("\\cC"), // control
        CG("\\cG"); // control

        String value;

        ValidEscapeInput(String value) {
            this.value = value;
        }
    }

    @ParameterizedTest
    @EnumSource(ValidEscapeInput.class)
    void validEscape(ValidEscapeInput input) {
        assertDoesNotThrow(() -> new JoniRegularExpression(input.value));
    }

    @Test
    void invalidPropertyName() {
        assertThrows(SyntaxException.class, () -> new JoniRegularExpression("\\p"));
        assertThrows(SyntaxException.class, () -> new JoniRegularExpression("\\P"));
        assertThrows(SyntaxException.class, () -> new JoniRegularExpression("\\pa"));
        assertThrows(SyntaxException.class, () -> new JoniRegularExpression("\\Pa"));
    }

    /**
     * Named capturing group: (?<name>...).
     * 
     * @see org.joni.constants.SyntaxProperties#OP2_QMARK_LT_NAMED_GROUP
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Named_capturing_group">Named
     *      capturing group</a>
     */
    @Test
    void namedCapturingGroup() {
        RegularExpression regex = new JoniRegularExpression("((?<OrgOID>[^,. ]+)\\s*\\.\\s*(?<AOID>[^,. ]+))(?:\\s*,\\s*)?");
        assertTrue(regex.matches("FFFF.12645,AAAA.6456"));
    }

    @Test
    void invalidNamedCapturingGroup() {
        assertThrows(RuntimeException.class, () -> new JoniRegularExpression("(?<name>)(?<name>)"));
    }

    /**
     * Named capturing group: (?<name>...).
     * 
     * @see org.joni.constants.SyntaxProperties#OP2_ESC_K_NAMED_BACKREF
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Named_backreference">Named
     *      backreference</a>
     */
    @Test
    void namedBackreference() {
        RegularExpression regex = new JoniRegularExpression("title=(?<quote>[\"'])(.*?)\\k<quote>");
        assertTrue(regex.matches("title=\"Named capturing groups\\' advantages\""));
    }

    @Test
    @Disabled // This test should pass but currently doesn't see issue #495
    void anchorShouldNotMatchMultilineInput() {
        RegularExpression regex = new JoniRegularExpression("^[a-z]{1,10}$");
        assertFalse(regex.matches("abc\n"));
    }

    /**
     * This test is because the JDK regex matches function implicitly adds anchors
     * which isn't expected.
     */
    @Test
    void noImplicitAnchors() {
        RegularExpression regex = new JoniRegularExpression("[a-z]{1,10}");
        assertTrue(regex.matches("1abc1"));
    }
}
