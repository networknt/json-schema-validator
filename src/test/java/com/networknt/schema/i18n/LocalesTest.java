/*
 * Copyright (c) 2023 the original author or authors.
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
package com.networknt.schema.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class LocalesTest {

    @Test
    void unsupportedShouldReturnLocaleRoot() {
        Locale result = Locales.findSupported("en-US;q=0.9,en-GB;q=1.0");
        assertEquals("", result.getLanguage());
    }

    @Test
    void shouldReturnHigherPriority() {
        Locale result = Locales.findSupported("zh-CN;q=0.9,zh-TW;q=1.0");
        assertEquals("zh-TW", result.toLanguageTag());
    }

    @Test
    void shouldReturnHigherPriorityToo() {
        Locale result = Locales.findSupported("zh-CN;q=1.0,zh-TW;q=0.9");
        assertEquals("zh-CN", result.toLanguageTag());
    }

    @Test
    void shouldReturnFound() {
        Locale result = Locales.findSupported("zh-SG;q=1.0,zh-TW;q=0.9");
        assertEquals("zh-TW", result.toLanguageTag());
    }

    @Test
    void shouldReturnFounds() {
        Locale result = Locales.findSupported("zh;q=1.0");
        assertEquals("zh", result.getLanguage());
    }
}
