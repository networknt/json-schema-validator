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

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Resolves locale specific messages.
 */
@FunctionalInterface
public interface MessageSource {
    /**
     * Gets the message.
     *
     * @param key                    to look up the message
     * @param defaultMessageSupplier the default message
     * @param locale                 the locale to use
     * @param args                   the message arguments
     * @return the message
     */
    String getMessage(String key, Supplier<String> defaultMessageSupplier, Locale locale, Object... args);

    /**
     * Gets the message.
     *
     * @param key            to look up the message
     * @param defaultMessage the default message
     * @param locale         the locale to use
     * @param args           the message arguments
     * @return the message
     */
    default String getMessage(String key, String defaultMessage, Locale locale, Object... args) {
        return getMessage(key, defaultMessage::toString, locale, args);
    }

    /**
     * Gets the message.
     *
     * @param key    to look up the message
     * @param locale the locale to use
     * @param args   the message arguments
     * @return the message
     */
    default String getMessage(String key, Locale locale, Object... args) {
        return getMessage(key, (Supplier<String>) null, locale, args);
    }
}
