/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.SpecificationVersionRange;
import com.networknt.schema.SchemaContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FunctionalInterface
interface ValidatorFactory {
    KeywordValidator newInstance(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext);
}

public enum KeywordType implements Keyword {
    ADDITIONAL_PROPERTIES("additionalProperties", AdditionalPropertiesValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    ALL_OF("allOf", AllOfValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    ANY_OF("anyOf", AnyOfValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    CONST("const", ConstValidator::new, SpecificationVersionRange.DRAFT_6_TO_DRAFT_7),
    CONTAINS("contains", ContainsValidator::new, SpecificationVersionRange.DRAFT_6_TO_DRAFT_7),
    CONTENT_ENCODING("contentEncoding", ContentEncodingValidator::new, SpecificationVersionRange.DRAFT_7),
    CONTENT_MEDIA_TYPE("contentMediaType", ContentMediaTypeValidator::new, SpecificationVersionRange.DRAFT_7),
    DEPENDENCIES("dependencies", DependenciesValidator::new, SpecificationVersionRange.ALL_VERSIONS),
    DEPENDENT_REQUIRED("dependentRequired", DependentRequired::new, SpecificationVersionRange.NONE),
    DEPENDENT_SCHEMAS("dependentSchemas", DependentSchemas::new, SpecificationVersionRange.NONE),
    DISCRIMINATOR("discriminator", DiscriminatorValidator::new, SpecificationVersionRange.NONE),
    DYNAMIC_REF("$dynamicRef", DynamicRefValidator::new, SpecificationVersionRange.NONE),
    ENUM("enum", EnumValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    EXCLUSIVE_MAXIMUM("exclusiveMaximum", ExclusiveMaximumValidator::new, SpecificationVersionRange.DRAFT_6_TO_DRAFT_7),
    EXCLUSIVE_MINIMUM("exclusiveMinimum", ExclusiveMinimumValidator::new, SpecificationVersionRange.DRAFT_6_TO_DRAFT_7),
    FALSE("false", FalseValidator::new, SpecificationVersionRange.MIN_DRAFT_6),
    FORMAT("format", null, SpecificationVersionRange.MAX_DRAFT_7) {
        @Override public KeywordValidator newValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
            throw new UnsupportedOperationException("Use FormatKeyword instead");
        }
    },
    ID("id", null, SpecificationVersionRange.ALL_VERSIONS),
    IF_THEN_ELSE("if", IfValidator::new, SpecificationVersionRange.DRAFT_7),
    ITEMS("items", ItemsValidator::new, SpecificationVersionRange.NONE),
    ITEMS_LEGACY("items", ItemsLegacyValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    MAX_CONTAINS("maxContains",MinMaxContainsValidator::new, SpecificationVersionRange.NONE),
    MAX_ITEMS("maxItems", MaxItemsValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    MAX_LENGTH("maxLength", MaxLengthValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    MAX_PROPERTIES("maxProperties", MaxPropertiesValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    MAXIMUM("maximum", MaximumValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    MIN_CONTAINS("minContains", MinMaxContainsValidator::new, SpecificationVersionRange.NONE),
    MIN_ITEMS("minItems", MinItemsValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    MIN_LENGTH("minLength", MinLengthValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    MIN_PROPERTIES("minProperties", MinPropertiesValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    MINIMUM("minimum", MinimumValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    MULTIPLE_OF("multipleOf", MultipleOfValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    NOT_ALLOWED("notAllowed", NotAllowedValidator::new, SpecificationVersionRange.ALL_VERSIONS),
    NOT("not", NotValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    ONE_OF("oneOf", OneOfValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    PATTERN_PROPERTIES("patternProperties", PatternPropertiesValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    PATTERN("pattern", PatternValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    PREFIX_ITEMS("prefixItems", PrefixItemsValidator::new, SpecificationVersionRange.NONE),
    PROPERTIES("properties", PropertiesValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    PROPERTY_DEPENDENCIES("propertyDependencies", PropertyDependenciesValidator::new, SpecificationVersionRange.NONE),
    PROPERTY_NAMES("propertyNames", PropertyNamesValidator::new, SpecificationVersionRange.DRAFT_6_TO_DRAFT_7),
    READ_ONLY("readOnly", ReadOnlyValidator::new, SpecificationVersionRange.DRAFT_7),
    RECURSIVE_REF("$recursiveRef", RecursiveRefValidator::new, SpecificationVersionRange.NONE),
    REF("$ref", RefValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    REQUIRED("required", RequiredValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    TRUE("true", TrueValidator::new, SpecificationVersionRange.MIN_DRAFT_6),
    TYPE("type", TypeValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    UNEVALUATED_ITEMS("unevaluatedItems", UnevaluatedItemsValidator::new, SpecificationVersionRange.NONE),
    UNEVALUATED_PROPERTIES("unevaluatedProperties",UnevaluatedPropertiesValidator::new,SpecificationVersionRange.NONE),
    UNION_TYPE("unionType", UnionTypeValidator::new, SpecificationVersionRange.NONE),
    UNIQUE_ITEMS("uniqueItems", UniqueItemsValidator::new, SpecificationVersionRange.MAX_DRAFT_7),
    WRITE_ONLY("writeOnly", WriteOnlyValidator::new, SpecificationVersionRange.DRAFT_7),
    ;

    private static final Map<String, KeywordType> CONSTANTS = new HashMap<>();

    static {
        for (KeywordType c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;
    private final ValidatorFactory validatorFactory;
    private final SpecificationVersionRange specificationVersionRange;

    KeywordType(String value, ValidatorFactory validatorFactory, SpecificationVersionRange specificationVersionRange) {
        this.value = value;
        this.validatorFactory = validatorFactory;
        this.specificationVersionRange = specificationVersionRange;
    }

    public static List<KeywordType> getKeywords(SpecificationVersion specificationVersion) {
        final List<KeywordType> result = new ArrayList<>();
        for (KeywordType keyword : values()) {
            if (keyword.getSpecificationVersionRange().getVersions().contains(specificationVersion)) {
                result.add(keyword);
            }
        }
        return result;
    }

    public static KeywordType fromValue(String value) {
        KeywordType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        }
        return constant;
    }

    @Override
    public KeywordValidator newValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        if (this.validatorFactory == null) {
            throw new UnsupportedOperationException("No suitable validator for " + getValue());
        }
        return validatorFactory.newInstance(schemaLocation, schemaNode, parentSchema,
                schemaContext);
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public SpecificationVersionRange getSpecificationVersionRange() {
        return this.specificationVersionRange;
    }
}
