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

import org.junit.jupiter.api.Test;

import java.util.Locale;

class ResourceBundleMessageSourceTest {

    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource("jsv-messages", "test-messages");

    @Test
    void messageNoDefault() {
        String message = messageSource.getMessage("unknown.key", Locale.getDefault());
        assertEquals("unknown.key", message);
    }

    @Test
    void messageDefaultSupplier() {
        String message = messageSource.getMessage("unknown.key", "default", Locale.getDefault());
        assertEquals("default", message);
    }

    @Test
    void messageDefaultSupplierArguments() {
        String message = messageSource.getMessage("unknown.key", "An error {0}", Locale.getDefault(), "argument");
        assertEquals("An error argument", message);
    }

    @Test
    void messageFound() {
        String message = messageSource.getMessage("atmostOne", Locale.getDefault());
        assertEquals("english", message);
    }

    @Test
    void messageFallbackOnDefaultLocale() {
        String message = messageSource.getMessage("atmostOne", Locale.SIMPLIFIED_CHINESE);
        assertEquals("english", message);
    }

    @Test
    void messageFrench() {
        String message = messageSource.getMessage("atmostOne", Locale.FRANCE);
        assertEquals("french", message);
    }

    @Test
    void messageMaxItems() {
        String message = messageSource.getMessage("maxItems", Locale.getDefault(), 5, 10);
        assertEquals("must have at most 5 items but found 10", message);
    }

    @Test
    void missingBundleShouldNotThrow() {
        MessageSource messageSource = new ResourceBundleMessageSource("missing-bundle");
        assertEquals("missing", messageSource.getMessage("missing", Locale.getDefault()));
    }

    @Test
    void overrideMessage() {
        MessageSource messageSource = new ResourceBundleMessageSource("jsv-messages-override", "jsv-messages");
        assertEquals("path: overridden message value", messageSource.getMessage("allOf", Locale.ROOT, "path", "value"));
        assertEquals("path: overridden message value", messageSource.getMessage("allOf", Locale.FRENCH, "path", "value"));
        assertEquals("must be valid to any of the schemas value", messageSource.getMessage("anyOf", Locale.ROOT, "value"));
    }
}
