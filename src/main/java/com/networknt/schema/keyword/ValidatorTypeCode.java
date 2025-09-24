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
import com.networknt.schema.SchemaContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FunctionalInterface
interface ValidatorFactory {
    KeywordValidator newInstance(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext);
}

enum VersionCode {
    None(new SpecificationVersion[] { }),
    AllVersions(new SpecificationVersion[] { SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MinV6(new SpecificationVersion[] { SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MinV6MaxV7(new SpecificationVersion[] { SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7 }),
    MinV7(new SpecificationVersion[] { SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MaxV7(new SpecificationVersion[] { SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7 }),
    MaxV201909(new SpecificationVersion[] { SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09 }),
    MinV201909(new SpecificationVersion[] { SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MinV202012(new SpecificationVersion[] { SpecificationVersion.DRAFT_2020_12 }),
    V201909(new SpecificationVersion[] { SpecificationVersion.DRAFT_2019_09 }),
    V7(new SpecificationVersion[] { SpecificationVersion.DRAFT_7 });

    private final EnumSet<SpecificationVersion> versions;

    VersionCode(SpecificationVersion[] versionFlags) {
        this.versions = EnumSet.noneOf(SpecificationVersion.class);
	      this.versions.addAll(Arrays.asList(versionFlags));
    }

    EnumSet<SpecificationVersion> getVersions() {
        return this.versions;
    }
}

public enum ValidatorTypeCode implements Keyword {
    ADDITIONAL_PROPERTIES("additionalProperties", AdditionalPropertiesValidator::new, VersionCode.MaxV7),
    ALL_OF("allOf", AllOfValidator::new, VersionCode.MaxV7),
    ANY_OF("anyOf", AnyOfValidator::new, VersionCode.MaxV7),
    CONST("const", ConstValidator::new, VersionCode.MinV6MaxV7),
    CONTAINS("contains", ContainsValidator::new, VersionCode.MinV6MaxV7),
    CONTENT_ENCODING("contentEncoding", ContentEncodingValidator::new, VersionCode.V7),
    CONTENT_MEDIA_TYPE("contentMediaType", ContentMediaTypeValidator::new, VersionCode.V7),
    DEPENDENCIES("dependencies", DependenciesValidator::new, VersionCode.AllVersions),
    DEPENDENT_REQUIRED("dependentRequired", DependentRequired::new, VersionCode.None),
    DEPENDENT_SCHEMAS("dependentSchemas", DependentSchemas::new, VersionCode.None),
    DISCRIMINATOR("discriminator", DiscriminatorValidator::new, VersionCode.None),
    DYNAMIC_REF("$dynamicRef", DynamicRefValidator::new, VersionCode.None),
    ENUM("enum", EnumValidator::new, VersionCode.MaxV7),
    EXCLUSIVE_MAXIMUM("exclusiveMaximum", ExclusiveMaximumValidator::new, VersionCode.MinV6MaxV7),
    EXCLUSIVE_MINIMUM("exclusiveMinimum", ExclusiveMinimumValidator::new, VersionCode.MinV6MaxV7),
    FALSE("false", FalseValidator::new, VersionCode.MinV6),
    FORMAT("format", null, VersionCode.MaxV7) {
        @Override public KeywordValidator newValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
            throw new UnsupportedOperationException("Use FormatKeyword instead");
        }
    },
    ID("id", null, VersionCode.AllVersions),
    IF_THEN_ELSE("if", IfValidator::new, VersionCode.V7),
    ITEMS_202012("items", ItemsValidator202012::new, VersionCode.None),
    ITEMS("items", ItemsValidator::new, VersionCode.MaxV7),
    MAX_CONTAINS("maxContains",MinMaxContainsValidator::new, VersionCode.None),
    MAX_ITEMS("maxItems", MaxItemsValidator::new, VersionCode.MaxV7),
    MAX_LENGTH("maxLength", MaxLengthValidator::new, VersionCode.MaxV7),
    MAX_PROPERTIES("maxProperties", MaxPropertiesValidator::new, VersionCode.MaxV7),
    MAXIMUM("maximum", MaximumValidator::new, VersionCode.MaxV7),
    MIN_CONTAINS("minContains", MinMaxContainsValidator::new, VersionCode.None),
    MIN_ITEMS("minItems", MinItemsValidator::new, VersionCode.MaxV7),
    MIN_LENGTH("minLength", MinLengthValidator::new, VersionCode.MaxV7),
    MIN_PROPERTIES("minProperties", MinPropertiesValidator::new, VersionCode.MaxV7),
    MINIMUM("minimum", MinimumValidator::new, VersionCode.MaxV7),
    MULTIPLE_OF("multipleOf", MultipleOfValidator::new, VersionCode.MaxV7),
    NOT_ALLOWED("notAllowed", NotAllowedValidator::new, VersionCode.AllVersions),
    NOT("not", NotValidator::new, VersionCode.MaxV7),
    ONE_OF("oneOf", OneOfValidator::new, VersionCode.MaxV7),
    PATTERN_PROPERTIES("patternProperties", PatternPropertiesValidator::new, VersionCode.MaxV7),
    PATTERN("pattern", PatternValidator::new, VersionCode.MaxV7),
    PREFIX_ITEMS("prefixItems", PrefixItemsValidator::new, VersionCode.None),
    PROPERTIES("properties", PropertiesValidator::new, VersionCode.MaxV7),
    PROPERTYNAMES("propertyNames", PropertyNamesValidator::new, VersionCode.MinV6MaxV7),
    READ_ONLY("readOnly", ReadOnlyValidator::new, VersionCode.V7),
    RECURSIVE_REF("$recursiveRef", RecursiveRefValidator::new, VersionCode.None),
    REF("$ref", RefValidator::new, VersionCode.MaxV7),
    REQUIRED("required", RequiredValidator::new, VersionCode.MaxV7),
    TRUE("true", TrueValidator::new, VersionCode.MinV6),
    TYPE("type", TypeValidator::new, VersionCode.MaxV7),
    UNEVALUATED_ITEMS("unevaluatedItems", UnevaluatedItemsValidator::new, VersionCode.None),
    UNEVALUATED_PROPERTIES("unevaluatedProperties",UnevaluatedPropertiesValidator::new,VersionCode.None),
    UNION_TYPE("unionType", UnionTypeValidator::new, VersionCode.None),
    UNIQUE_ITEMS("uniqueItems", UniqueItemsValidator::new, VersionCode.MaxV7),
    WRITE_ONLY("writeOnly", WriteOnlyValidator::new, VersionCode.V7),
    ;

    private static final Map<String, ValidatorTypeCode> CONSTANTS = new HashMap<>();

    static {
        for (ValidatorTypeCode c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;
    private final ValidatorFactory validatorFactory;
    private final VersionCode versionCode;

    ValidatorTypeCode(String value, ValidatorFactory validatorFactory, VersionCode versionCode) {
        this.value = value;
        this.validatorFactory = validatorFactory;
        this.versionCode = versionCode;
    }

    public static List<ValidatorTypeCode> getKeywords(SpecificationVersion versionFlag) {
        final List<ValidatorTypeCode> result = new ArrayList<>();
        for (ValidatorTypeCode keyword : values()) {
            if (keyword.getVersionCode().getVersions().contains(versionFlag)) {
                result.add(keyword);
            }
        }
        return result;
    }

    public static ValidatorTypeCode fromValue(String value) {
        ValidatorTypeCode constant = CONSTANTS.get(value);
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

    public VersionCode getVersionCode() {
        return this.versionCode;
    }
}
