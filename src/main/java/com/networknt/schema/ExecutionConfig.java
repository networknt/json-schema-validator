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

package com.networknt.schema;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Configuration per execution.
 */
public class ExecutionConfig {
    /**
     * The locale to use for formatting messages.
     */
    private Locale locale = Locale.ROOT;

    /**
     * Determines if annotation collection is enabled.
     * <p>
     * This does not affect annotation collection required for evaluating keywords
     * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
     */
    private boolean annotationCollectionEnabled = false;

    /**
     * If annotation collection is enabled, determine which annotations to collect.
     * <p>
     * This does not affect annotation collection required for evaluating keywords
     * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
     */
    private Predicate<String> annotationCollectionFilter = keyword -> false;

    /**
     * Since Draft 2019-09 format assertions are not enabled by default.
     */
    private Boolean formatAssertionsEnabled = null;

    /**
     * Determine if the validation execution can fail fast.
     */
    private boolean failFast = false;

    /**
     * Determine if debugging features such that logging are switched on.
     * <p>
     * This is turned off by default. This is present because the library attempts
     * to log debug logs at each validation node and the logger evaluation on
     * whether the logger is turned on is impacting performance.
     */
    private boolean debugEnabled = false;

    /**
     * Gets the locale to use for formatting messages.
     * 
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale to use for formatting messages.
     * 
     * @param locale the locale
     */
    public void setLocale(Locale locale) {
        this.locale = Objects.requireNonNull(locale, "Locale must not be null");
    }

    /**
     * Gets the format assertion enabled flag.
     * <p>
     * This defaults to null meaning that it will follow the defaults of the
     * specification.
     * <p>
     * Since draft 2019-09 this will default to false unless enabled by using the
     * $vocabulary keyword.
     * 
     * @return the format assertions enabled flag
     */
    public Boolean getFormatAssertionsEnabled() {
        return formatAssertionsEnabled;
    }

    /**
     * Sets the format assertion enabled flag.
     * 
     * @param formatAssertionsEnabled the format assertions enabled flag
     */
    public void setFormatAssertionsEnabled(Boolean formatAssertionsEnabled) {
        this.formatAssertionsEnabled = formatAssertionsEnabled;
    }

    /**
     * Return if fast fail is enabled.
     * 
     * @return if fast fail is enabled
     */
    public boolean isFailFast() {
        return failFast;
    }

    /**
     * Sets whether fast fail is enabled.
     * 
     * @param failFast true to fast fail
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    /**
     * Return if annotation collection is enabled.
     * <p>
     * This does not affect annotation collection required for evaluating keywords
     * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
     * <p>
     * The annotations to collect can be customized using the annotation collection
     * predicate.
     * 
     * @return if annotation collection is enabled
     */
    public boolean isAnnotationCollectionEnabled() {
        return annotationCollectionEnabled;
    }

    /**
     * Sets whether the annotation collection is enabled.
     * <p>
     * This does not affect annotation collection required for evaluating keywords
     * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
     * <p>
     * The annotations to collect can be customized using the annotation collection
     * predicate.
     * 
     * @param annotationCollectionEnabled true to enable annotation collection
     */
    public void setAnnotationCollectionEnabled(boolean annotationCollectionEnabled) {
        this.annotationCollectionEnabled = annotationCollectionEnabled;
    }

    /**
     * Gets the predicate to determine if annotation collection is allowed for a
     * particular keyword. This only has an effect if annotation collection is
     * enabled.
     * <p>
     * The default value is to not collect any annotation keywords if annotation
     * collection is enabled.
     * <p>
     * This does not affect annotation collection required for evaluating keywords
     * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
     * 
     * @return the predicate to determine if annotation collection is allowed for
     *         the keyword
     */
    public Predicate<String> getAnnotationCollectionFilter() {
        return annotationCollectionFilter;
    }

    /**
     * Predicate to determine if annotation collection is allowed for a particular
     * keyword. This only has an effect if annotation collection is enabled.
     * <p>
     * The default value is to not collect any annotation keywords if annotation
     * collection is enabled.
     * <p>
     * This does not affect annotation collection required for evaluating keywords
     * such as unevaluatedItems or unevaluatedProperties and only affects reporting.
     *
     * @param annotationCollectionFilter the predicate accepting the keyword
     */
    public void setAnnotationCollectionFilter(Predicate<String> annotationCollectionFilter) {
        this.annotationCollectionFilter = Objects.requireNonNull(annotationCollectionFilter,
                "annotationCollectionFilter must not be null");
    }

    /**
     * Gets if debugging features such as logging is switched on.
     *
     * @return true if debug is enabled
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Sets if debugging features such as logging is switched on.
     *
     * @param debugEnabled true to enable debug
     */
    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }
}
