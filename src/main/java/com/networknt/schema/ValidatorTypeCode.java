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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FunctionalInterface
interface ValidatorFactory {
    JsonValidator newInstance(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext);
}

enum VersionCode {
    AllVersions(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V4, SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV6(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV7(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MaxV201909(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V4, SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909 }),
    MinV201909(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV202012(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V202012 }),
    V201909(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V201909 });

    private final EnumSet<VersionFlag> versions;

    VersionCode(SpecVersion.VersionFlag[] versionFlags) {
    	this.versions = EnumSet.noneOf(VersionFlag.class);
        for (VersionFlag flag: versionFlags) {
        	this.versions.add(flag);
        }
    }

    EnumSet<VersionFlag> getVersions() {
		return this.versions;
	}
}

public enum ValidatorTypeCode implements Keyword, ErrorMessageType {
    ADDITIONAL_PROPERTIES("additionalProperties", "1001", AdditionalPropertiesValidator::new, VersionCode.AllVersions),
    ALL_OF("allOf", "1002", AllOfValidator::new, VersionCode.AllVersions),
    ANY_OF("anyOf", "1003", AnyOfValidator::new, VersionCode.AllVersions),
    CONST("const", "1042", ConstValidator::new, VersionCode.MinV6),
    CONTAINS("contains", "1043", ContainsValidator::new, VersionCode.MinV6),
    CROSS_EDITS("crossEdits", "1004", null, VersionCode.AllVersions),
    DATETIME("dateTime", "1034", null, VersionCode.AllVersions),
    DEPENDENCIES("dependencies", "1007", DependenciesValidator::new, VersionCode.AllVersions),
    DEPENDENT_REQUIRED("dependentRequired", "1045", DependentRequired::new, VersionCode.MinV201909),
    DEPENDENT_SCHEMAS("dependentSchemas", "1046", DependentSchemas::new, VersionCode.MinV201909),
    EDITS("edits", "1005", null, VersionCode.AllVersions),
    ENUM("enum", "1008", EnumValidator::new, VersionCode.AllVersions),
    EXCLUSIVE_MAXIMUM("exclusiveMaximum", "1038", ExclusiveMaximumValidator::new, VersionCode.MinV6),
    EXCLUSIVE_MINIMUM("exclusiveMinimum", "1039", ExclusiveMinimumValidator::new, VersionCode.MinV6),
    FALSE("false", "1041", FalseValidator::new, VersionCode.MinV6),
    FORMAT("format", "1009", null, VersionCode.AllVersions) {
        @Override public JsonValidator newValidator(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
            throw new UnsupportedOperationException("Use FormatKeyword instead");
        }
    },
    ID("id", "1036", null, VersionCode.AllVersions),
    IF_THEN_ELSE("if", "1037", IfValidator::new, VersionCode.MinV7),
    ITEMS_202012("items", "1010", ItemsValidator202012::new, VersionCode.MinV202012),
    ITEMS("items", "1010", ItemsValidator::new, VersionCode.MaxV201909),
    MAX_CONTAINS("maxContains", "1006", MinMaxContainsValidator::new, VersionCode.MinV201909),
    MAX_ITEMS("maxItems", "1012", MaxItemsValidator::new, VersionCode.AllVersions),
    MAX_LENGTH("maxLength", "1013", MaxLengthValidator::new, VersionCode.AllVersions),
    MAX_PROPERTIES("maxProperties", "1014", MaxPropertiesValidator::new, VersionCode.AllVersions),
    MAXIMUM("maximum", "1011", MaximumValidator::new, VersionCode.AllVersions),
    MIN_CONTAINS("minContains", "1049", MinMaxContainsValidator::new, VersionCode.MinV201909),
    MIN_ITEMS("minItems", "1016", MinItemsValidator::new, VersionCode.AllVersions),
    MIN_LENGTH("minLength", "1017", MinLengthValidator::new, VersionCode.AllVersions),
    MIN_PROPERTIES("minProperties", "1018", MinPropertiesValidator::new, VersionCode.AllVersions),
    MINIMUM("minimum", "1015", MinimumValidator::new, VersionCode.AllVersions),
    MULTIPLE_OF("multipleOf", "1019", MultipleOfValidator::new, VersionCode.AllVersions),
    NOT_ALLOWED("notAllowed", "1033", NotAllowedValidator::new, VersionCode.AllVersions),
    NOT("not", "1020", NotValidator::new, VersionCode.AllVersions),
    ONE_OF("oneOf", "1022", OneOfValidator::new, VersionCode.AllVersions),
    PATTERN_PROPERTIES("patternProperties", "1024", PatternPropertiesValidator::new, VersionCode.AllVersions),
    PATTERN("pattern", "1023", PatternValidator::new, VersionCode.AllVersions),
    PREFIX_ITEMS("prefixItems", "1048", PrefixItemsValidator::new, VersionCode.MinV202012),
    PROPERTIES("properties", "1025", PropertiesValidator::new, VersionCode.AllVersions),
    PROPERTYNAMES("propertyNames", "1044", PropertyNamesValidator::new, VersionCode.MinV6),
    READ_ONLY("readOnly", "1032", ReadOnlyValidator::new, VersionCode.MinV7),
    RECURSIVE_REF("$recursiveRef", "1050", RecursiveRefValidator::new, VersionCode.V201909),
    REF("$ref", "1026", RefValidator::new, VersionCode.AllVersions),
    REQUIRED("required", "1028", RequiredValidator::new, VersionCode.AllVersions),
    TRUE("true", "1040", TrueValidator::new, VersionCode.MinV6),
    TYPE("type", "1029", TypeValidator::new, VersionCode.AllVersions),
    UNEVALUATED_ITEMS("unevaluatedItems", "1021", UnevaluatedItemsValidator::new, VersionCode.MinV201909),
    UNEVALUATED_PROPERTIES("unevaluatedProperties","1047",UnevaluatedPropertiesValidator::new,VersionCode.MinV6),
    UNION_TYPE("unionType", "1030", UnionTypeValidator::new, VersionCode.AllVersions),
    UNIQUE_ITEMS("uniqueItems", "1031", UniqueItemsValidator::new, VersionCode.AllVersions),
    UUID("uuid", "1035", null, VersionCode.AllVersions),
    WRITE_ONLY("writeOnly", "1027", WriteOnlyValidator::new, VersionCode.MinV7),
    ;

    private static final Map<String, ValidatorTypeCode> CONSTANTS = new HashMap<>();

    static {
        for (ValidatorTypeCode c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;
    private final String errorCode;
    private Map<String, String> customMessage;
    private final String errorCodeKey;
    private final ValidatorFactory validatorFactory;
    private final VersionCode versionCode;


    private ValidatorTypeCode(String value, String errorCode, ValidatorFactory validatorFactory, VersionCode versionCode) {
        this.value = value;
        this.errorCode = errorCode;
        this.errorCodeKey = value + "ErrorCode";
        this.validatorFactory = validatorFactory;
        this.versionCode = versionCode;
        this.customMessage = null;
    }

    public static List<ValidatorTypeCode> getNonFormatKeywords(SpecVersion.VersionFlag versionFlag) {
        final List<ValidatorTypeCode> result = new ArrayList<>();
        for (ValidatorTypeCode keyword : values()) {
            if (!FORMAT.equals(keyword) && keyword.getVersionCode().getVersions().contains(versionFlag)) {
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
    public JsonValidator newValidator(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
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

    @Override
    public void setCustomMessage(Map<String, String> message) {
        this.customMessage = message;
    }

    @Override
    public Map<String, String> getCustomMessage() {
        return this.customMessage;
    }

    public String getErrorCodeKey() {
        return this.errorCodeKey;
    }

    public VersionCode getVersionCode() {
        return this.versionCode;
    }

    @Override
    public String getErrorCodeValue() {
        return getValue();
    }
}
