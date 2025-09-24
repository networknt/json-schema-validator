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
import com.networknt.schema.NodePath;
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
    KeywordValidator newInstance(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext);
}

public enum Keywords implements Keyword {
    ADDITIONAL_PROPERTIES("additionalProperties", AdditionalPropertiesValidator::new, SpecificationVersionRange.MaxV7),
    ALL_OF("allOf", AllOfValidator::new, SpecificationVersionRange.MaxV7),
    ANY_OF("anyOf", AnyOfValidator::new, SpecificationVersionRange.MaxV7),
    CONST("const", ConstValidator::new, SpecificationVersionRange.MinV6MaxV7),
    CONTAINS("contains", ContainsValidator::new, SpecificationVersionRange.MinV6MaxV7),
    CONTENT_ENCODING("contentEncoding", ContentEncodingValidator::new, SpecificationVersionRange.V7),
    CONTENT_MEDIA_TYPE("contentMediaType", ContentMediaTypeValidator::new, SpecificationVersionRange.V7),
    DEPENDENCIES("dependencies", DependenciesValidator::new, SpecificationVersionRange.AllVersions),
    DEPENDENT_REQUIRED("dependentRequired", DependentRequired::new, SpecificationVersionRange.None),
    DEPENDENT_SCHEMAS("dependentSchemas", DependentSchemas::new, SpecificationVersionRange.None),
    DISCRIMINATOR("discriminator", DiscriminatorValidator::new, SpecificationVersionRange.None),
    DYNAMIC_REF("$dynamicRef", DynamicRefValidator::new, SpecificationVersionRange.None),
    ENUM("enum", EnumValidator::new, SpecificationVersionRange.MaxV7),
    EXCLUSIVE_MAXIMUM("exclusiveMaximum", ExclusiveMaximumValidator::new, SpecificationVersionRange.MinV6MaxV7),
    EXCLUSIVE_MINIMUM("exclusiveMinimum", ExclusiveMinimumValidator::new, SpecificationVersionRange.MinV6MaxV7),
    FALSE("false", FalseValidator::new, SpecificationVersionRange.MinV6),
    FORMAT("format", null, SpecificationVersionRange.MaxV7) {
        @Override public KeywordValidator newValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
            throw new UnsupportedOperationException("Use FormatKeyword instead");
        }
    },
    ID("id", null, SpecificationVersionRange.AllVersions),
    IF_THEN_ELSE("if", IfValidator::new, SpecificationVersionRange.V7),
    ITEMS_202012("items", ItemsValidator202012::new, SpecificationVersionRange.None),
    ITEMS("items", ItemsValidator::new, SpecificationVersionRange.MaxV7),
    MAX_CONTAINS("maxContains",MinMaxContainsValidator::new, SpecificationVersionRange.None),
    MAX_ITEMS("maxItems", MaxItemsValidator::new, SpecificationVersionRange.MaxV7),
    MAX_LENGTH("maxLength", MaxLengthValidator::new, SpecificationVersionRange.MaxV7),
    MAX_PROPERTIES("maxProperties", MaxPropertiesValidator::new, SpecificationVersionRange.MaxV7),
    MAXIMUM("maximum", MaximumValidator::new, SpecificationVersionRange.MaxV7),
    MIN_CONTAINS("minContains", MinMaxContainsValidator::new, SpecificationVersionRange.None),
    MIN_ITEMS("minItems", MinItemsValidator::new, SpecificationVersionRange.MaxV7),
    MIN_LENGTH("minLength", MinLengthValidator::new, SpecificationVersionRange.MaxV7),
    MIN_PROPERTIES("minProperties", MinPropertiesValidator::new, SpecificationVersionRange.MaxV7),
    MINIMUM("minimum", MinimumValidator::new, SpecificationVersionRange.MaxV7),
    MULTIPLE_OF("multipleOf", MultipleOfValidator::new, SpecificationVersionRange.MaxV7),
    NOT_ALLOWED("notAllowed", NotAllowedValidator::new, SpecificationVersionRange.AllVersions),
    NOT("not", NotValidator::new, SpecificationVersionRange.MaxV7),
    ONE_OF("oneOf", OneOfValidator::new, SpecificationVersionRange.MaxV7),
    PATTERN_PROPERTIES("patternProperties", PatternPropertiesValidator::new, SpecificationVersionRange.MaxV7),
    PATTERN("pattern", PatternValidator::new, SpecificationVersionRange.MaxV7),
    PREFIX_ITEMS("prefixItems", PrefixItemsValidator::new, SpecificationVersionRange.None),
    PROPERTIES("properties", PropertiesValidator::new, SpecificationVersionRange.MaxV7),
    PROPERTYNAMES("propertyNames", PropertyNamesValidator::new, SpecificationVersionRange.MinV6MaxV7),
    READ_ONLY("readOnly", ReadOnlyValidator::new, SpecificationVersionRange.V7),
    RECURSIVE_REF("$recursiveRef", RecursiveRefValidator::new, SpecificationVersionRange.None),
    REF("$ref", RefValidator::new, SpecificationVersionRange.MaxV7),
    REQUIRED("required", RequiredValidator::new, SpecificationVersionRange.MaxV7),
    TRUE("true", TrueValidator::new, SpecificationVersionRange.MinV6),
    TYPE("type", TypeValidator::new, SpecificationVersionRange.MaxV7),
    UNEVALUATED_ITEMS("unevaluatedItems", UnevaluatedItemsValidator::new, SpecificationVersionRange.None),
    UNEVALUATED_PROPERTIES("unevaluatedProperties",UnevaluatedPropertiesValidator::new,SpecificationVersionRange.None),
    UNION_TYPE("unionType", UnionTypeValidator::new, SpecificationVersionRange.None),
    UNIQUE_ITEMS("uniqueItems", UniqueItemsValidator::new, SpecificationVersionRange.MaxV7),
    WRITE_ONLY("writeOnly", WriteOnlyValidator::new, SpecificationVersionRange.V7),
    ;

    private static final Map<String, Keywords> CONSTANTS = new HashMap<>();

    static {
        for (Keywords c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;
    private final ValidatorFactory validatorFactory;
    private final SpecificationVersionRange versionCode;

    Keywords(String value, ValidatorFactory validatorFactory, SpecificationVersionRange versionCode) {
        this.value = value;
        this.validatorFactory = validatorFactory;
        this.versionCode = versionCode;
    }

    public static List<Keywords> getKeywords(SpecificationVersion versionFlag) {
        final List<Keywords> result = new ArrayList<>();
        for (Keywords keyword : values()) {
            if (keyword.getVersionCode().getVersions().contains(versionFlag)) {
                result.add(keyword);
            }
        }
        return result;
    }

    public static Keywords fromValue(String value) {
        Keywords constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        }
        return constant;
    }

    @Override
    public KeywordValidator newValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        if (this.validatorFactory == null) {
            throw new UnsupportedOperationException("No suitable validator for " + getValue());
        }
        return validatorFactory.newInstance(schemaLocation, evaluationPath, schemaNode, parentSchema,
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

    public SpecificationVersionRange getVersionCode() {
        return this.versionCode;
    }
}
