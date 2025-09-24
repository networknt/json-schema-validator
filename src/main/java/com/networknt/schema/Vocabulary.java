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

import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.Keyword;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.KeywordType;

/**
 * Represents a vocabulary in meta-schema.
 * <p>
 * This contains the IRI and the keywords in the vocabulary.
 */
public class Vocabulary {

    // 2019-09
    public static final Vocabulary DRAFT_2019_09_CORE = new Vocabulary("https://json-schema.org/draft/2019-09/vocab/core",
            new NonValidationKeyword("$id"), new NonValidationKeyword("$schema"), new NonValidationKeyword("$anchor"),
            KeywordType.REF, KeywordType.RECURSIVE_REF, new NonValidationKeyword("$recursiveAnchor"),
            new NonValidationKeyword("$vocabulary"), new NonValidationKeyword("$comment"),
            new NonValidationKeyword("$defs"));
    public static final Vocabulary DRAFT_2019_09_APPLICATOR = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/applicator", new NonValidationKeyword("additionalItems"),
            KeywordType.UNEVALUATED_ITEMS, KeywordType.ITEMS, KeywordType.CONTAINS,
            KeywordType.ADDITIONAL_PROPERTIES, KeywordType.UNEVALUATED_PROPERTIES,
            KeywordType.PROPERTIES, KeywordType.PATTERN_PROPERTIES, KeywordType.DEPENDENT_SCHEMAS,
            KeywordType.PROPERTYNAMES, KeywordType.IF_THEN_ELSE, new NonValidationKeyword("then"),
            new NonValidationKeyword("else"), KeywordType.ALL_OF, KeywordType.ANY_OF,
            KeywordType.ONE_OF, KeywordType.NOT);
    public static final Vocabulary DRAFT_2019_09_VALIDATION = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/validation", KeywordType.MULTIPLE_OF,
            KeywordType.MAXIMUM, KeywordType.EXCLUSIVE_MAXIMUM, KeywordType.MINIMUM,
            KeywordType.EXCLUSIVE_MINIMUM, KeywordType.MAX_LENGTH, KeywordType.MIN_LENGTH,
            KeywordType.PATTERN, KeywordType.MAX_ITEMS, KeywordType.MIN_ITEMS,
            KeywordType.UNIQUE_ITEMS, KeywordType.MAX_CONTAINS, KeywordType.MIN_CONTAINS,
            KeywordType.MAX_PROPERTIES, KeywordType.MIN_PROPERTIES, KeywordType.REQUIRED,
            KeywordType.DEPENDENT_REQUIRED, KeywordType.CONST, KeywordType.ENUM,
            KeywordType.TYPE);
    public static final Vocabulary DRAFT_2019_09_META_DATA = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/meta-data", new AnnotationKeyword("title"),
            new AnnotationKeyword("description"), new AnnotationKeyword("default"), new AnnotationKeyword("deprecated"),
            KeywordType.READ_ONLY, KeywordType.WRITE_ONLY, new AnnotationKeyword("examples"));
    public static final Vocabulary DRAFT_2019_09_FORMAT = new Vocabulary("https://json-schema.org/draft/2019-09/vocab/format",
            KeywordType.FORMAT);
    public static final Vocabulary DRAFT_2019_09_CONTENT = new Vocabulary(
            "https://json-schema.org/draft/2019-09/vocab/content", new AnnotationKeyword("contentMediaType"),
            new AnnotationKeyword("contentEncoding"), new AnnotationKeyword("contentSchema"));

    // 2020-12
    public static final Vocabulary DRAFT_2020_12_CORE = new Vocabulary("https://json-schema.org/draft/2020-12/vocab/core",
            new NonValidationKeyword("$id"), new NonValidationKeyword("$schema"), KeywordType.REF,
            new NonValidationKeyword("$anchor"), KeywordType.DYNAMIC_REF,
            new NonValidationKeyword("$dynamicAnchor"), new NonValidationKeyword("$vocabulary"),
            new NonValidationKeyword("$comment"), new NonValidationKeyword("$defs"));
    public static final Vocabulary DRAFT_2020_12_APPLICATOR = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/applicator", KeywordType.PREFIX_ITEMS,
            KeywordType.ITEMS_202012, KeywordType.CONTAINS, KeywordType.ADDITIONAL_PROPERTIES,
            KeywordType.PROPERTIES, KeywordType.PATTERN_PROPERTIES, KeywordType.DEPENDENT_SCHEMAS,
            KeywordType.PROPERTYNAMES, KeywordType.IF_THEN_ELSE, new NonValidationKeyword("then"),
            new NonValidationKeyword("else"), KeywordType.ALL_OF, KeywordType.ANY_OF,
            KeywordType.ONE_OF, KeywordType.NOT);
    public static final Vocabulary DRAFT_2020_12_UNEVALUATED = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/unevaluated", KeywordType.UNEVALUATED_ITEMS,
            KeywordType.UNEVALUATED_PROPERTIES);
    public static final Vocabulary DRAFT_2020_12_VALIDATION = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/validation", KeywordType.TYPE, KeywordType.CONST,
            KeywordType.ENUM, KeywordType.MULTIPLE_OF, KeywordType.MAXIMUM,
            KeywordType.EXCLUSIVE_MAXIMUM, KeywordType.MINIMUM, KeywordType.EXCLUSIVE_MINIMUM,
            KeywordType.MAX_LENGTH, KeywordType.MIN_LENGTH, KeywordType.PATTERN,
            KeywordType.MAX_ITEMS, KeywordType.MIN_ITEMS, KeywordType.UNIQUE_ITEMS,
            KeywordType.MAX_CONTAINS, KeywordType.MIN_CONTAINS, KeywordType.MAX_PROPERTIES,
            KeywordType.MIN_PROPERTIES, KeywordType.REQUIRED, KeywordType.DEPENDENT_REQUIRED);
    public static final Vocabulary DRAFT_2020_12_META_DATA = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/meta-data", new AnnotationKeyword("title"),
            new AnnotationKeyword("description"), new AnnotationKeyword("default"), new AnnotationKeyword("deprecated"),
            KeywordType.READ_ONLY, KeywordType.WRITE_ONLY, new AnnotationKeyword("examples"));
    public static final Vocabulary DRAFT_2020_12_FORMAT_ANNOTATION = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/format-annotation", KeywordType.FORMAT);
    public static final Vocabulary DRAFT_2020_12_FORMAT_ASSERTION = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/format-assertion", KeywordType.FORMAT);
    public static final Vocabulary DRAFT_2020_12_CONTENT = new Vocabulary(
            "https://json-schema.org/draft/2020-12/vocab/content", new AnnotationKeyword("contentEncoding"),
            new AnnotationKeyword("contentMediaType"), new AnnotationKeyword("contentSchema"));

    // OpenAPI 3.1
    public static final Vocabulary OPENAPI_3_1_BASE = new Vocabulary("https://spec.openapis.org/oas/3.1/vocab/base",
            new AnnotationKeyword("example"), KeywordType.DISCRIMINATOR, new AnnotationKeyword("externalDocs"),
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
