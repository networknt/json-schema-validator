/*
 * Copyright (c) 2026 the original author or authors.
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
package com.networknt.schema.format;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HostnameFormatTest {

    private static final HostnameFormat FORMAT = new HostnameFormat();

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    private static boolean matches(String value) {
        return FORMAT.matches(null, value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "example",
        "example.com",
        "a.b-c.d1",
        "xn--bcher-kva.example",
        "XN--BCHER-KVA.EXAMPLE",
        "xn--Bcher-kva.example",
        "xn--ls8h.la",
        "xn--e28h.example",
        "xn--bd6h.example"
    })
    void accepts(String value) {
        assertTrue(matches(value), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "example.com.",
        ".example.com",
        "a..b",
        "-example.com",
        "example-.com",
        "exa mple.com",
        "xn--X",
        "xn--abc-",
        "xn--hello-txk.example"
    })
    void rejects(String value) {
        assertFalse(matches(value), value);
    }

    @Test
    void limitsTheNameTo253Characters() {
        String label = repeat("a", 63);
        String name253 = label + "." + label + "." + label + "." + repeat("a", 61);
        assertTrue(matches(name253));
        assertFalse(matches(name253 + "a"));
    }

    @Test
    void limitsEachLabelTo63Characters() {
        assertTrue(matches(repeat("a", 63) + ".com"));
        assertFalse(matches(repeat("a", 64) + ".com"));
        assertFalse(matches("xn--" + repeat("a", 60) + ".com"));
    }
}
