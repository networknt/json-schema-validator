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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for JDKRegularExpression.
 */
class JDKRegularExpressionTest {
    @Test
    void namedCapturingGroup() {
        RegularExpression regex = new JDKRegularExpression("((?<OrgOID>[^,. ]+)\\s*\\.\\s*(?<AOID>[^,. ]+))(?:\\s*,\\s*)?");
        assertTrue(regex.matches("FFFF.12645,AAAA.6456"));
    }

    @Test
    void invalidNamedCapturingGroup() {
        assertThrows(RuntimeException.class, () -> new JDKRegularExpression("(?<name>)(?<name>)"));
    }

    @Test
    void namedBackreference() {
        RegularExpression regex = new JDKRegularExpression("title=(?<quote>[\"'])(.*?)\\k<quote>");
        assertTrue(regex.matches("title=\"Named capturing groups\\' advantages\""));
    }

    @Test
    @Disabled
    void anchorShouldNotMatchMultilineInput() {
        RegularExpression regex = new JDKRegularExpression("^[a-z]{1,10}$");
        assertFalse(regex.matches("abc\n"));
    }

    /**
     * This test is because the JDK regex matches function implicitly adds anchors
     * which isn't expected.
     */
    @Test
    void noImplicitAnchors() {
        RegularExpression regex = new JDKRegularExpression("[a-z]{1,10}");
        assertTrue(regex.matches("1abc1"));
    }
}
