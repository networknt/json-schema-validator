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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Locale.FilteringMode;
import java.util.Locale.LanguageRange;
import java.util.stream.Collectors;

/**
 * Functions for working with Locales.
 */
public class Locales {
    /**
     * The list of locale resource bundles.
     */
    public static final String[] SUPPORTED_LANGUAGE_TAGS = new String[] { "ar-EG", "cs-CZ", "da-DK", "de", "fa-IR",
            "fi-FI", "fr-CA", "fr", "he-IL", "hr-HR", "hu-HU", "it", "ja-JP", "ko-KR", "nb-NO", "nl-NL", "pl-PL",
            "pt-BR", "ro-RO", "ru-RU", "sk-SK", "sv-SE", "th-TH", "tr-TR", "uk-UA", "vi-VN", "zh-CN", "zh-TW" };

    /**
     * The supported locales.
     */
    public static final List<Locale> SUPPORTED_LOCALES = of(SUPPORTED_LANGUAGE_TAGS);

    /**
     * Gets the supported locales.
     * 
     * @return the supported locales
     */
    public static List<Locale> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    /**
     * Gets a list of {@link Locale} by language tags.
     * 
     * @param languageTags for the locales
     * @return the locales
     */
    public static List<Locale> of(String... languageTags) {
        return Arrays.asList(languageTags).stream().map(Locale::forLanguageTag).collect(Collectors.toList());
    }

    /**
     * Determine the best matching {@link Locale} with respect to the priority list.
     * 
     * @param priorityList the language tag priority list
     * @return the best matching locale
     */
    public static Locale findSupported(String priorityList) {
        return findSupported(priorityList, getSupportedLocales());
    }

    /**
     * Determine the best matching {@link Locale} with respect to the priority list.
     * 
     * @param priorityList the language tag priority list
     * @param locales      the supported locales
     * @return the best matching locale
     */
    public static Locale findSupported(String priorityList, Collection<Locale> locales) {
        return findSupported(LanguageRange.parse(priorityList), locales, FilteringMode.AUTOSELECT_FILTERING);
    }

    /**
     * Determine the best matching {@link Locale} with respect to the priority list.
     * 
     * @param priorityList  the language tag priority list
     * @param locales       the supported locales
     * @param filteringMode the filtering mode
     * @return the best matching locale
     */
    public static Locale findSupported(List<LanguageRange> priorityList, Collection<Locale> locales,
            FilteringMode filteringMode) {
        Locale result = Locale.lookup(priorityList, locales);
        if (result != null) {
            return result;
        }
        List<Locale> matching = Locale.filter(priorityList, locales, filteringMode);
        if (!matching.isEmpty()) {
            return matching.get(0);
        }
        return Locale.ROOT;
    }
}
