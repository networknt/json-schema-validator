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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Annotations.
 */
public class Annotations {
    public static final Set<String> UNEVALUATED_PROPERTIES_ANNOTATIONS;
    public static final Set<String> UNEVALUATED_ITEMS_ANNOTATIONS;
    public static final Set<String> EVALUATION_ANNOTATIONS;

    public static final Predicate<String> UNEVALUATED_PROPERTIES_ANNOTATIONS_PREDICATE;
    public static final Predicate<String> UNEVALUATED_ITEMS_ANNOTATIONS_PREDICATE;
    public static final Predicate<String> EVALUATION_ANNOTATIONS_PREDICATE;
    public static final Predicate<String> PREDICATE_FALSE;

    static {
        Set<String> unevaluatedProperties = new HashSet<>();
        unevaluatedProperties.add("unevaluatedProperties");
        unevaluatedProperties.add("properties");
        unevaluatedProperties.add("patternProperties");
        unevaluatedProperties.add("additionalProperties");
        UNEVALUATED_PROPERTIES_ANNOTATIONS = Collections.unmodifiableSet(unevaluatedProperties);

        Set<String> unevaluatedItems = new HashSet<>();
        unevaluatedItems.add("unevaluatedItems");
        unevaluatedItems.add("items");
        unevaluatedItems.add("prefixItems");
        unevaluatedItems.add("additionalItems");
        unevaluatedItems.add("contains");
        UNEVALUATED_ITEMS_ANNOTATIONS = Collections.unmodifiableSet(unevaluatedItems);

        Set<String> evaluation = new HashSet<>();
        evaluation.addAll(unevaluatedProperties);
        evaluation.addAll(unevaluatedItems);
        EVALUATION_ANNOTATIONS = Collections.unmodifiableSet(evaluation);

        UNEVALUATED_PROPERTIES_ANNOTATIONS_PREDICATE = UNEVALUATED_PROPERTIES_ANNOTATIONS::contains;
        UNEVALUATED_ITEMS_ANNOTATIONS_PREDICATE = UNEVALUATED_ITEMS_ANNOTATIONS::contains;
        EVALUATION_ANNOTATIONS_PREDICATE = EVALUATION_ANNOTATIONS::contains;
        PREDICATE_FALSE = (keyword) -> false;
    }

    /**
     * Gets the default annotation allow list.
     * 
     * @param metaSchema the meta schema
     */
    public static Set<String> getDefaultAnnotationAllowList(JsonMetaSchema metaSchema) {
        boolean unevaluatedProperties = metaSchema.getKeywords().get("unevaluatedProperties") != null;
        boolean unevaluatedItems = metaSchema.getKeywords().get("unevaluatedItems") != null;
        if (unevaluatedProperties && unevaluatedItems) {
            return EVALUATION_ANNOTATIONS;
        } else if (unevaluatedProperties && !unevaluatedItems) {
            return UNEVALUATED_PROPERTIES_ANNOTATIONS;
        } else if (!unevaluatedProperties && unevaluatedItems) {
            return UNEVALUATED_ITEMS_ANNOTATIONS;
        }
        return Collections.emptySet();
    }

    /**
     * Gets the default annotation allow list predicate.
     * 
     * @param metaSchema the meta schema
     */
    public static Predicate<String> getDefaultAnnotationAllowListPredicate(JsonMetaSchema metaSchema) {
        boolean unevaluatedProperties = metaSchema.getKeywords().get("unevaluatedProperties") != null;
        boolean unevaluatedItems = metaSchema.getKeywords().get("unevaluatedItems") != null;
        if (unevaluatedProperties && unevaluatedItems) {
            return EVALUATION_ANNOTATIONS_PREDICATE;
        } else if (unevaluatedProperties && !unevaluatedItems) {
            return UNEVALUATED_PROPERTIES_ANNOTATIONS_PREDICATE;
        } else if (!unevaluatedProperties && unevaluatedItems) {
            return UNEVALUATED_ITEMS_ANNOTATIONS_PREDICATE;
        }
        return PREDICATE_FALSE;
    }
}
