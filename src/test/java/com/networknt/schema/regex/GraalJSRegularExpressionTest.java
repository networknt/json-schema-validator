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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test for GraalJSRegularExpression.
 */
class GraalJSRegularExpressionTest {
    private static final GraalJSRegularExpressionContext CONTEXT = new GraalJSRegularExpressionContext(
            GraalJSContextFactory.getInstance());

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
        RuntimeException e = assertThrows(RuntimeException.class, () -> new GraalJSRegularExpression(input.value, CONTEXT));
        assertTrue(e.getMessage().startsWith("SyntaxError"));
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
        assertDoesNotThrow(() -> new GraalJSRegularExpression(input.value, CONTEXT));
    }
    
    @Test
    void invalidPropertyName() {
        assertThrows(RuntimeException.class, () -> new GraalJSRegularExpression("\\p", CONTEXT));
        assertThrows(RuntimeException.class, () -> new GraalJSRegularExpression("\\P", CONTEXT));
        assertThrows(RuntimeException.class, () -> new GraalJSRegularExpression("\\pa", CONTEXT));
        assertThrows(RuntimeException.class, () -> new GraalJSRegularExpression("\\Pa", CONTEXT));
    }
    
    @Test
    void digit() {
        RegularExpression regex = new GraalJSRegularExpression("\\d", CONTEXT);
        assertTrue(regex.matches("1"));
        assertFalse(regex.matches("a"));
    }

    @Test
    void invalidEscape() {
        RuntimeException e = assertThrows(RuntimeException.class, () -> new GraalJSRegularExpression("\\a", CONTEXT));
        assertEquals("SyntaxError: Invalid escape", e.getMessage());
    }

    @Test
    void namedCapturingGroup() {
        RegularExpression regex = new GraalJSRegularExpression("((?<OrgOID>[^,. ]+)\\s*\\.\\s*(?<AOID>[^,. ]+))(?:\\s*,\\s*)?", CONTEXT);
        assertTrue(regex.matches("FFFF.12645,AAAA.6456"));
    }

    @Test
    void invalidNamedCapturingGroup() {
        assertThrows(RuntimeException.class, () -> new GraalJSRegularExpression("(?<name>)(?<name>)", CONTEXT));
    }

    @Test
    void namedBackreference() {
        RegularExpression regex = new GraalJSRegularExpression("title=(?<quote>[\"'])(.*?)\\k<quote>", CONTEXT);
        assertTrue(regex.matches("title=\"Named capturing groups\\' advantages\""));
    }

    @Test
    void anchorShouldNotMatchMultilineInput() {
        RegularExpression regex = new GraalJSRegularExpression("^[a-z]{1,10}$", CONTEXT);
        assertFalse(regex.matches("abc\n"));
    }

    /**
     * This test is because the JDK regex matches function implicitly adds anchors
     * which isn't expected.
     */
    @Test
    void noImplicitAnchors() {
        RegularExpression regex = new GraalJSRegularExpression("[a-z]{1,10}", CONTEXT);
        assertTrue(regex.matches("1abc1"));
    }


    @Test
    void concurrency() throws Exception {
        RegularExpression regex = new GraalJSRegularExpression("\\d", CONTEXT);
        Exception[] instance = new Exception[1];
        CountDownLatch latch = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 50; ++i) {
            Runnable runner = new Runnable() {
                public void run() {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        assertTrue(regex.matches("1"));
                    } catch (RuntimeException e) {
                        instance[0] = e;
                    }
                }
            };
            Thread thread = new Thread(runner, "Thread" + i);
            thread.start();
            threads.add(thread);
        }
        latch.countDown(); // Release the latch for threads to run concurrently
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        if (instance[0] != null) {
            throw instance[0];
        }
    }
}
