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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a vocabulary in meta-schema.
 * <p>
 * This contains the IRI and the keywords in the vocabulary.
 */
public class Vocabulary {

    // 2019-09
    public static final Vocabulary V201909_CORE = new Vocabulary("https://json-schema.org/draft/2019-09/vocab/core",
            new NonValidationKeyword("$id"), new NonValidationKeyword("$schema"), new NonValidationKeyword("$anchor"),
            ValidatorTypeCode.REF, ValidatorTypeCode.RECURSIVE_REF, new NonValidationKeyword("$recursiveAnchor"),
            new NonValidationKeyword("$vocabulary"), new NonValidationKeyword("$comment"),
            new NonValidationKeyword("$defs"));
    public static final Vocabulary V201909_APPLICATOR = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/applicator", new NonValidationKeyword("additionalItems"),
            ValidatorTypeCode.UNEVALUATED_ITEMS, ValidatorTypeCode.ITEMS, ValidatorTypeCode.CONTAINS,
            ValidatorTypeCode.ADDITIONAL_PROPERTIES, ValidatorTypeCode.UNEVALUATED_PROPERTIES,
            ValidatorTypeCode.PROPERTIES, ValidatorTypeCode.PATTERN_PROPERTIES, ValidatorTypeCode.DEPENDENT_SCHEMAS,
            ValidatorTypeCode.PROPERTYNAMES, ValidatorTypeCode.IF_THEN_ELSE, new NonValidationKeyword("then"),
            new NonValidationKeyword("else"), ValidatorTypeCode.ALL_OF, ValidatorTypeCode.ANY_OF,
            ValidatorTypeCode.ONE_OF, ValidatorTypeCode.NOT);
    public static final Vocabulary V201909_VALIDATION = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/validation", ValidatorTypeCode.MULTIPLE_OF,
            ValidatorTypeCode.MAXIMUM, ValidatorTypeCode.EXCLUSIVE_MAXIMUM, ValidatorTypeCode.MINIMUM,
            ValidatorTypeCode.EXCLUSIVE_MINIMUM, ValidatorTypeCode.MAX_LENGTH, ValidatorTypeCode.MIN_LENGTH,
            ValidatorTypeCode.PATTERN, ValidatorTypeCode.MAX_ITEMS, ValidatorTypeCode.MIN_ITEMS,
            ValidatorTypeCode.UNIQUE_ITEMS, ValidatorTypeCode.MAX_CONTAINS, ValidatorTypeCode.MIN_CONTAINS,
            ValidatorTypeCode.MAX_PROPERTIES, ValidatorTypeCode.MIN_PROPERTIES, ValidatorTypeCode.REQUIRED,
            ValidatorTypeCode.DEPENDENT_REQUIRED, ValidatorTypeCode.CONST, ValidatorTypeCode.ENUM,
            ValidatorTypeCode.TYPE);
    public static final Vocabulary V201909_META_DATA = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/meta-data", new AnnotationKeyword("title"),
            new AnnotationKeyword("description"), new AnnotationKeyword("default"), new AnnotationKeyword("deprecated"),
            ValidatorTypeCode.READ_ONLY, ValidatorTypeCode.WRITE_ONLY, new AnnotationKeyword("examples"));
    public static final Vocabulary V201909_FORMAT = new Vocabulary("https://json-schema.org/draft/2019-09/vocab/format",
            ValidatorTypeCode.FORMAT);
    public static final Vocabulary V201909_CONTENT = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/content", new AnnotationKeyword("contentMediaType"),
            new AnnotationKeyword("contentEncoding"), new AnnotationKeyword("contentSchema"));

    // 2020-12
    public static final Vocabulary V202012_CORE = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/core",
            new NonValidationKeyword("$id"), new NonValidationKeyword("$schema"), ValidatorTypeCode.REF,
            new NonValidationKeyword("$anchor"), ValidatorTypeCode.DYNAMIC_REF,
            new NonValidationKeyword("$dynamicAnchor"), new NonValidationKeyword("$vocabulary"),
            new NonValidationKeyword("$comment"), new NonValidationKeyword("$defs"));
    public static final Vocabulary V202012_APPLICATOR = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/applicator", ValidatorTypeCode.PREFIX_ITEMS,
            ValidatorTypeCode.ITEMS_202012, ValidatorTypeCode.CONTAINS, ValidatorTypeCode.ADDITIONAL_PROPERTIES,
            ValidatorTypeCode.PROPERTIES, ValidatorTypeCode.PATTERN_PROPERTIES, ValidatorTypeCode.DEPENDENT_SCHEMAS,
            ValidatorTypeCode.PROPERTYNAMES, ValidatorTypeCode.IF_THEN_ELSE, new NonValidationKeyword("then"),
            new NonValidationKeyword("else"), ValidatorTypeCode.ALL_OF, ValidatorTypeCode.ANY_OF,
            ValidatorTypeCode.ONE_OF, ValidatorTypeCode.NOT);
    public static final Vocabulary V202012_UNEVALUATED = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/unevaluated", ValidatorTypeCode.UNEVALUATED_ITEMS,
            ValidatorTypeCode.UNEVALUATED_PROPERTIES);
    public static final Vocabulary V202012_VALIDATION = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/validation", ValidatorTypeCode.TYPE, ValidatorTypeCode.CONST,
            ValidatorTypeCode.ENUM, ValidatorTypeCode.MULTIPLE_OF, ValidatorTypeCode.MAXIMUM,
            ValidatorTypeCode.EXCLUSIVE_MAXIMUM, ValidatorTypeCode.MINIMUM, ValidatorTypeCode.EXCLUSIVE_MINIMUM,
            ValidatorTypeCode.MAX_LENGTH, ValidatorTypeCode.MIN_LENGTH, ValidatorTypeCode.PATTERN,
            ValidatorTypeCode.MAX_ITEMS, ValidatorTypeCode.MIN_ITEMS, ValidatorTypeCode.UNIQUE_ITEMS,
            ValidatorTypeCode.MAX_CONTAINS, ValidatorTypeCode.MIN_CONTAINS, ValidatorTypeCode.MAX_PROPERTIES,
            ValidatorTypeCode.MIN_PROPERTIES, ValidatorTypeCode.REQUIRED, ValidatorTypeCode.DEPENDENT_REQUIRED);
    public static final Vocabulary V202012_META_DATA = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/meta-data", new AnnotationKeyword("title"),
            new AnnotationKeyword("description"), new AnnotationKeyword("default"), new AnnotationKeyword("deprecated"),
            ValidatorTypeCode.READ_ONLY, ValidatorTypeCode.WRITE_ONLY, new AnnotationKeyword("examples"));
    public static final Vocabulary V202012_FORMAT_ANNOTATION = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/format-annotation", ValidatorTypeCode.FORMAT);
    public static final Vocabulary V202012_FORMAT_ASSERTION = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/format-assertion", ValidatorTypeCode.FORMAT);
    public static final Vocabulary V202012_CONTENT = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/content", new AnnotationKeyword("contentEncoding"),
            new AnnotationKeyword("contentMediaType"), new AnnotationKeyword("contentSchema"));

    // OpenAPI 3.1
    public static final Vocabulary OPENAPI_3_1_BASE = new Vocabulary("https://spec.openapis.org/oas/3.1/vocab/base",
            new AnnotationKeyword("example"), ValidatorTypeCode.DISCRIMINATOR, new AnnotationKeyword("externalDocs"),
            new AnnotationKeyword("xml"));

    private final String iri;
    private final Set<Keyword> keywords;

    /**
     * Constructor.
     * 
     * @param iri       the iri
     * @param keywords the keywords
     */
    public Vocabulary(String iri, Keyword... keywords) {
        this.iri = iri;
        this.keywords = new LinkedHashSet<>();
	      this.keywords.addAll(Arrays.asList(keywords));
    }

    /**
     * The iri of the vocabulary.
     * 
     * @return the iri
     */
    public String getIri() {
        return iri;
    }

    /**
     * The keywords in the vocabulary.
     * 
     * @return the keywords
     */
    public Set<Keyword> getKeywords() {
        return keywords;
    }

    @Override
    public int hashCode() {
        return Objects.hash(iri, keywords);
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
        return Objects.equals(iri, other.iri) && Objects.equals(keywords, other.keywords);
    }

    @Override
    public String toString() {
        return "Vocabulary [iri=" + iri + ", keywords=" + keywords + "]";
    }

}
