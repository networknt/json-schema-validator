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
package com.networknt.schema;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a vocabulary in meta schema.
 * <p>
 * This contains the id and the related keywords.
 */
public class Vocabulary {

    // 2019-09
    public static final Vocabulary V201909_CORE = new Vocabulary("https://json-schema.org/draft/2019-09/vocab/core",
            "$id", "$schema", "$anchor", "$ref", "$recursiveRef", "$recursiveAnchor", "$vocabulary", "$comment",
            "$defs");
    public static final Vocabulary V201909_APPLICATOR = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/applicator", "additionalItems", "unevaluatedItems", "items",
            "contains", "additionalProperties", "unevaluatedProperties", "properties", "patternProperties",
            "dependentSchemas", "propertyNames", "if", "then", "else", "allOf", "anyOf", "oneOf", "not");
    public static final Vocabulary V201909_VALIDATION = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/validation", "multipleOf", "maximum", "exclusiveMaximum",
            "minimum", "exclusiveMinimum", "maxLength", "minLength", "pattern", "maxItems", "minItems", "uniqueItems",
            "maxContains", "minContains", "maxProperties", "minProperties", "required", "dependentRequired", "const",
            "enum", "type");
    public static final Vocabulary V201909_META_DATA = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/meta-data", "title", "description", "default", "deprecated",
            "readOnly", "writeOnly", "examples");
    public static final Vocabulary V201909_FORMAT = new Vocabulary("https://json-schema.org/draft/2019-09/vocab/format",
            "format");
    public static final Vocabulary V201909_CONTENT = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/content", "contentMediaType", "contentEncoding",
            "contentSchema");

    // 2020-12
    public static final Vocabulary V202012_CORE = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/core",
            "$id", "$schema", "$ref", "$anchor", "$dynamicRef", "$dynamicAnchor", "$vocabulary", "$comment", "$defs");
    public static final Vocabulary V202012_APPLICATOR = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/applicator", "prefixItems", "items", "contains",
            "additionalProperties", "properties", "patternProperties", "dependentSchemas", "propertyNames", "if",
            "then", "else", "allOf", "anyOf", "oneOf", "not");
    public static final Vocabulary V202012_UNEVALUATED = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/unevaluated", "unevaluatedItems", "unevaluatedProperties");
    public static final Vocabulary V202012_VALIDATION = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/validation", "type", "const", "enum", "multipleOf", "maximum",
            "exclusiveMaximum", "minimum", "exclusiveMinimum", "maxLength", "minLength", "pattern", "maxItems",
            "minItems", "uniqueItems", "maxContains", "minContains", "maxProperties", "minProperties", "required",
            "dependentRequired");
    public static final Vocabulary V202012_META_DATA = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/meta-data", "title", "description", "default", "deprecated",
            "readOnly", "writeOnly", "examples");
    public static final Vocabulary V202012_FORMAT_ANNOTATION = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/format-annotation", "format");
    public static final Vocabulary V202012_FORMAT_ASSERTION = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/format-assertion", "format");
    public static final Vocabulary V202012_CONTENT = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/content", "contentEncoding", "contentMediaType",
            "contentSchema");

    private final String id;
    private final Set<String> keywords;

    /**
     * Constructor.
     * 
     * @param id       the id
     * @param keywords the keywords
     */
    public Vocabulary(String id, String... keywords) {
        this.id = id;
        this.keywords = new LinkedHashSet<>();
        for (String keyword : keywords) {
            this.keywords.add(keyword);
        }
    }

    /**
     * The id of the vocabulary.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * The keywords in the vocabulary.
     * 
     * @return the keywords
     */
    public Set<String> getKeywords() {
        return keywords;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, keywords);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vocabulary other = (Vocabulary) obj;
        return Objects.equals(id, other.id) && Objects.equals(keywords, other.keywords);
    }

    @Override
    public String toString() {
        return "Vocabulary [id=" + id + ", keywords=" + keywords + "]";
    }

}
