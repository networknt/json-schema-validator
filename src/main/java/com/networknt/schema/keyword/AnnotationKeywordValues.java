/*
 * Copyright (c) 2026 Network New Technologies Inc.
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

package com.networknt.schema.keyword;

import tools.jackson.databind.JsonNode;

/**
 * Helpers for the keywords that carry an annotation value instead of, or in addition
 * to, an assertion.
 * <p>
 * The helpers are package private and static. They are used by
 * {@link AbstractKeywordValidator} for the keywords that only contribute an annotation,
 * and by {@link ReadOnlyValidator} and {@link WriteOnlyValidator} for the execution
 * policies that turn their value into an assertion. They are deliberately not part of
 * the API surface of the public validator base classes, which every keyword validator
 * inherits.
 */
final class AnnotationKeywordValues {
    private AnnotationKeywordValues() {
    }

    /**
     * Gets the value to report as an annotation from the schema node of a keyword.
     * <p>
     * The keywords that contribute an annotation do not all use the same value type.
     * The meta-data, content and OpenAPI vocabularies use strings, numbers, booleans,
     * arrays and objects, and a meta-data keyword can also be null, so every JSON value
     * type is mapped. Strings, numbers and booleans are unwrapped to their Java type.
     * An object or an array is reported as a deep copy of the schema's node, so the
     * value is a {@link JsonNode} that the caller may keep and mutate without affecting
     * the schema. A copy is a deep copy because a shallow one still shares the children
     * of the node, which are themselves mutable. The copy is made once per validator
     * rather than on every validation, which is what {@link #copyOf} is for. The
     * returned null in this method means that there is no value to report, which is
     * different from an annotation whose value is the JSON null.
     *
     * @param schemaNode the schema node of the keyword
     * @return the annotation value, or null if there is no value to report
     */
    static Object get(JsonNode schemaNode) {
        if (schemaNode.isString()) {
            return schemaNode.asString();
        } else if (schemaNode.isNumber()) {
            return schemaNode.numberValue();
        } else if (schemaNode.isBoolean()) {
            return schemaNode.booleanValue();
        } else if (schemaNode.isObject() || schemaNode.isArray() || schemaNode.isNull()) {
            // A copy of a meta-data value, an OpenAPI xml or example object, or a
            // contentSchema. A NullNode is immutable, so its copy is itself.
            return schemaNode.deepCopy();
        }
        return null;
    }

    /**
     * Returns whether the annotation value of the keyword is reported as a copy of the
     * schema node.
     * <p>
     * This is the case for an object and for an array. A string, a number, a boolean
     * and a null are not copied, so copying a validator's node before it is needed
     * would be wasted work for those values.
     *
     * @param schemaNode the schema node of the keyword
     * @return whether the value is reported as a copy of the node
     */
    static boolean isCopied(JsonNode schemaNode) {
        return schemaNode.isObject() || schemaNode.isArray();
    }

    /**
     * Gets a copy of the schema node to report as an annotation, for a validator to
     * build once and hold.
     * <p>
     * Copying on every validation costs the size of the node times the number of
     * validations, because a validator is shared by every instance that it validates,
     * and the node does not change. A single copy is enough to keep the schema safe
     * from a caller that mutates the value it was given.
     *
     * @param schemaNode the schema node of the keyword
     * @return the copy to report
     */
    static JsonNode copyOf(JsonNode schemaNode) {
        return schemaNode.deepCopy();
    }
}
