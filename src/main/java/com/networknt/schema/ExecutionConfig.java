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
    private Locale locale = Locale.ROOT;
    private Predicate<String> annotationAllowedPredicate = (keyword) -> true;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = Objects.requireNonNull(locale, "Locale must not be null");
    }

    /**
     * Gets the predicate to determine if annotation collection is allowed for a
     * particular keyword.
     * <p>
     * The default value is to allow annotation collection.
     * <p>
     * Setting this to return false improves performance but keywords such as
     * unevaluatedItems and unevaluatedProperties will fail to evaluate properly.
     * <p>
     * This will also affect reporting if annotations need to be in the output
     * format.
     * <p>
     * unevaluatedProperties depends on properties, patternProperties and
     * additionalProperties.
     * <p>
     * unevaluatedItems depends on items/prefixItems, additionalItems/items and
     * contains.
     * 
     * @return the predicate to determine if annotation collection is allowed for
     *         the keyword
     */
    public Predicate<String> getAnnotationAllowedPredicate() {
        return annotationAllowedPredicate;
    }

    /**
     * Predicate to determine if annotation collection is allowed for a particular
     * keyword.
     * <p>
     * The default value is to allow annotation collection.
     * <p>
     * Setting this to return false improves performance but keywords such as
     * unevaluatedItems and unevaluatedProperties will fail to evaluate properly.
     * <p>
     * This will also affect reporting if annotations need to be in the output
     * format.
     * <p>
     * unevaluatedProperties depends on properties, patternProperties and
     * additionalProperties.
     * <p>
     * unevaluatedItems depends on items/prefixItems, additionalItems/items and
     * contains.
     * 
     * @param annotationAllowedPredicate the predicate accepting the keyword
     */
    public void setAnnotationAllowedPredicate(Predicate<String> annotationAllowedPredicate) {
        this.annotationAllowedPredicate = Objects.requireNonNull(annotationAllowedPredicate,
                "annotationAllowedPredicate must not be null");
    }
}
