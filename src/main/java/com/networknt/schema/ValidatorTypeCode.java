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

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FunctionalInterface
interface ValidatorFactory {
    JsonValidator newInstance(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext);
}

enum VersionCode {
    None(new SpecVersion.VersionFlag[] { }),
    AllVersions(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V4, SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV6(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV6MaxV7(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7 }),
    MinV7(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MaxV7(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V4, SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7 }),
    MaxV201909(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V4, SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909 }),
    MinV201909(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV202012(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V202012 }),
    V201909(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V201909 }),
    V7(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V7 });

    private final EnumSet<VersionFlag> versions;

    VersionCode(SpecVersion.VersionFlag[] versionFlags) {
        this.versions = EnumSet.noneOf(VersionFlag.class);
	      this.versions.addAll(Arrays.asList(versionFlags));
    }

    EnumSet<VersionFlag> getVersions() {
        return this.versions;
    }
}

public enum ValidatorTypeCode implements Keyword, ErrorMessageType {
    ADDITIONAL_PROPERTIES("additionalProperties", "1001", AdditionalPropertiesValidator::new, VersionCode.MaxV7),
    ALL_OF("allOf", "1002", AllOfValidator::new, VersionCode.MaxV7),
    ANY_OF("anyOf", "1003", AnyOfValidator::new, VersionCode.MaxV7),
    CONST("const", "1042", ConstValidator::new, VersionCode.MinV6MaxV7),
    CONTAINS("contains", "1043", ContainsValidator::new, VersionCode.MinV6MaxV7),
    CONTENT_ENCODING("contentEncoding", "1052", ContentEncodingValidator::new, VersionCode.V7),
    CONTENT_MEDIA_TYPE("contentMediaType", "1053", ContentMediaTypeValidator::new, VersionCode.V7),
    DEPENDENCIES("dependencies", "1007", DependenciesValidator::new, VersionCode.AllVersions),
    DEPENDENT_REQUIRED("dependentRequired", "1045", DependentRequired::new, VersionCode.None),
    DEPENDENT_SCHEMAS("dependentSchemas", "1046", DependentSchemas::new, VersionCode.None),
    DISCRIMINATOR("discriminator", "2001", DiscriminatorValidator::new, VersionCode.None),
    DYNAMIC_REF("$dynamicRef", "1051", DynamicRefValidator::new, VersionCode.None),
    ENUM("enum", "1008", EnumValidator::new, VersionCode.MaxV7),
    EXCLUSIVE_MAXIMUM("exclusiveMaximum", "1038", ExclusiveMaximumValidator::new, VersionCode.MinV6MaxV7),
    EXCLUSIVE_MINIMUM("exclusiveMinimum", "1039", ExclusiveMinimumValidator::new, VersionCode.MinV6MaxV7),
    FALSE("false", "1041", FalseValidator::new, VersionCode.MinV6),
    FORMAT("format", "1009", null, VersionCode.MaxV7) {
        @Override public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
            throw new UnsupportedOperationException("Use FormatKeyword instead");
        }
    },
    ID("id", "1036", null, VersionCode.AllVersions),
    IF_THEN_ELSE("if", "1037", IfValidator::new, VersionCode.V7),
    ITEMS_202012("items", "1010", ItemsValidator202012::new, VersionCode.None),
    ITEMS("items", "1010", ItemsValidator::new, VersionCode.MaxV7),
    MAX_CONTAINS("maxContains", "1006", MinMaxContainsValidator::new, VersionCode.None),
    MAX_ITEMS("maxItems", "1012", MaxItemsValidator::new, VersionCode.MaxV7),
    MAX_LENGTH("maxLength", "1013", MaxLengthValidator::new, VersionCode.MaxV7),
    MAX_PROPERTIES("maxProperties", "1014", MaxPropertiesValidator::new, VersionCode.MaxV7),
    MAXIMUM("maximum", "1011", MaximumValidator::new, VersionCode.MaxV7),
    MIN_CONTAINS("minContains", "1049", MinMaxContainsValidator::new, VersionCode.None),
    MIN_ITEMS("minItems", "1016", MinItemsValidator::new, VersionCode.MaxV7),
    MIN_LENGTH("minLength", "1017", MinLengthValidator::new, VersionCode.MaxV7),
    MIN_PROPERTIES("minProperties", "1018", MinPropertiesValidator::new, VersionCode.MaxV7),
    MINIMUM("minimum", "1015", MinimumValidator::new, VersionCode.MaxV7),
    MULTIPLE_OF("multipleOf", "1019", MultipleOfValidator::new, VersionCode.MaxV7),
    NOT_ALLOWED("notAllowed", "1033", NotAllowedValidator::new, VersionCode.AllVersions),
    NOT("not", "1020", NotValidator::new, VersionCode.MaxV7),
    ONE_OF("oneOf", "1022", OneOfValidator::new, VersionCode.MaxV7),
    PATTERN_PROPERTIES("patternProperties", "1024", PatternPropertiesValidator::new, VersionCode.MaxV7),
    PATTERN("pattern", "1023", PatternValidator::new, VersionCode.MaxV7),
    PREFIX_ITEMS("prefixItems", "1048", PrefixItemsValidator::new, VersionCode.None),
    PROPERTIES("properties", "1025", PropertiesValidator::new, VersionCode.MaxV7),
    PROPERTYNAMES("propertyNames", "1044", PropertyNamesValidator::new, VersionCode.MinV6MaxV7),
    READ_ONLY("readOnly", "1032", ReadOnlyValidator::new, VersionCode.V7),
    RECURSIVE_REF("$recursiveRef", "1050", RecursiveRefValidator::new, VersionCode.None),
    REF("$ref", "1026", RefValidator::new, VersionCode.MaxV7),
    REQUIRED("required", "1028", RequiredValidator::new, VersionCode.MaxV7),
    TRUE("true", "1040", TrueValidator::new, VersionCode.MinV6),
    TYPE("type", "1029", TypeValidator::new, VersionCode.MaxV7),
    UNEVALUATED_ITEMS("unevaluatedItems", "1021", UnevaluatedItemsValidator::new, VersionCode.None),
    UNEVALUATED_PROPERTIES("unevaluatedProperties","1047",UnevaluatedPropertiesValidator::new,VersionCode.None),
    UNION_TYPE("unionType", "1030", UnionTypeValidator::new, VersionCode.None),
    UNIQUE_ITEMS("uniqueItems", "1031", UniqueItemsValidator::new, VersionCode.MaxV7),
    WRITE_ONLY("writeOnly", "1027", WriteOnlyValidator::new, VersionCode.V7),
    ;

    private static final Map<String, ValidatorTypeCode> CONSTANTS = new HashMap<>();

    static {
        for (ValidatorTypeCode c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;
    private final String errorCode;
    private final ValidatorFactory validatorFactory;
    private final VersionCode versionCode;

    ValidatorTypeCode(String value, String errorCode, ValidatorFactory validatorFactory, VersionCode versionCode) {
        this.value = value;
        this.errorCode = errorCode;
        this.validatorFactory = validatorFactory;
        this.versionCode = versionCode;
    }

    public static List<ValidatorTypeCode> getKeywords(SpecVersion.VersionFlag versionFlag) {
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
    public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext) {
        if (this.validatorFactory == null) {
            throw new UnsupportedOperationException("No suitable validator for " + getValue());
        }
        return validatorFactory.newInstance(schemaLocation, evaluationPath, schemaNode, parentSchema,
                validationContext);
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    public VersionCode getVersionCode() {
        return this.versionCode;
    }

    @Override
    public String getErrorCodeValue() {
        return getValue();
    }
}
