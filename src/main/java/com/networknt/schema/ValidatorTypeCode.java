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

import java.lang.reflect.Constructor;
import java.util.*;

enum VersionCode {
    AllVersions(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V4, SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV6(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV7(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MaxV201909(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V4, SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909 }),
    MinV201909(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV202012(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V202012 });

    private final EnumSet<VersionFlag> versions;

    VersionCode(SpecVersion.VersionFlag[] versionFlags) {
    	this.versions = EnumSet.noneOf(VersionFlag.class);
        for (VersionFlag flag: versionFlags) {
        	this.versions.add(flag);
        }
    }

    EnumSet<VersionFlag> getVersions() {
		return versions;
	}
}

public enum ValidatorTypeCode implements Keyword, ErrorMessageType {
    ADDITIONAL_PROPERTIES("additionalProperties", "1001", AdditionalPropertiesValidator.class, VersionCode.AllVersions),
    ALL_OF("allOf", "1002", AllOfValidator.class, VersionCode.AllVersions),
    ANY_OF("anyOf", "1003", AnyOfValidator.class, VersionCode.AllVersions),
    CROSS_EDITS("crossEdits", "1004", null, VersionCode.AllVersions),
    DEPENDENCIES("dependencies", "1007", DependenciesValidator.class, VersionCode.AllVersions),
    EDITS("edits", "1005", null, VersionCode.AllVersions),
    ENUM("enum", "1008", EnumValidator.class, VersionCode.AllVersions),
    FORMAT("format", "1009", null, VersionCode.AllVersions) {
        @Override
        public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext)
                throws Exception {
            throw new UnsupportedOperationException("Use FormatKeyword instead");
        }
    },
    ITEMS("items", "1010", ItemsValidator.class, VersionCode.MaxV201909),
    ITEMS_202012("items", "1010", ItemsValidator202012.class, VersionCode.MinV202012),
    MAXIMUM("maximum", "1011", MaximumValidator.class, VersionCode.AllVersions),
    MAX_ITEMS("maxItems", "1012", MaxItemsValidator.class, VersionCode.AllVersions),
    MAX_LENGTH("maxLength", "1013", MaxLengthValidator.class, VersionCode.AllVersions),
    MAX_PROPERTIES("maxProperties", "1014", MaxPropertiesValidator.class, VersionCode.AllVersions),
    MINIMUM("minimum", "1015", MinimumValidator.class, VersionCode.AllVersions),
    MIN_ITEMS("minItems", "1016", MinItemsValidator.class, VersionCode.AllVersions),
    MIN_LENGTH("minLength", "1017", MinLengthValidator.class, VersionCode.AllVersions),
    MIN_PROPERTIES("minProperties", "1018", MinPropertiesValidator.class, VersionCode.AllVersions),
    MULTIPLE_OF("multipleOf", "1019", MultipleOfValidator.class, VersionCode.AllVersions),
    NOT_ALLOWED("notAllowed", "1033", NotAllowedValidator.class, VersionCode.AllVersions),
    NOT("not", "1020", NotValidator.class, VersionCode.AllVersions),
    ONE_OF("oneOf", "1022", OneOfValidator.class, VersionCode.AllVersions),
    PATTERN_PROPERTIES("patternProperties", "1024", PatternPropertiesValidator.class, VersionCode.AllVersions),
    PATTERN("pattern", "1023", PatternValidator.class, VersionCode.AllVersions),
    PREFIX_ITEMS("prefixItems", "1048", PrefixItemsValidator.class, VersionCode.MinV202012),
    PROPERTIES("properties", "1025", PropertiesValidator.class, VersionCode.AllVersions),
    READ_ONLY("readOnly", "1032", ReadOnlyValidator.class, VersionCode.AllVersions),
    REF("$ref", "1026", RefValidator.class, VersionCode.AllVersions),
    REQUIRED("required", "1028", RequiredValidator.class, VersionCode.AllVersions),
    TYPE("type", "1029", TypeValidator.class, VersionCode.AllVersions),
    UNION_TYPE("unionType", "1030", UnionTypeValidator.class, VersionCode.AllVersions),
    UNIQUE_ITEMS("uniqueItems", "1031", UniqueItemsValidator.class, VersionCode.AllVersions),
    DATETIME("dateTime", "1034", null, VersionCode.AllVersions),
    UUID("uuid", "1035", null, VersionCode.AllVersions),
    ID("id", "1036", null, VersionCode.AllVersions),
    IF_THEN_ELSE("if", "1037", IfValidator.class, VersionCode.MinV7),
    EXCLUSIVE_MAXIMUM("exclusiveMaximum", "1038", ExclusiveMaximumValidator.class, VersionCode.MinV6),
    EXCLUSIVE_MINIMUM("exclusiveMinimum", "1039", ExclusiveMinimumValidator.class, VersionCode.MinV6),
    TRUE("true", "1040", TrueValidator.class, VersionCode.MinV6),
    FALSE("false", "1041", FalseValidator.class, VersionCode.MinV6),
    CONST("const", "1042", ConstValidator.class, VersionCode.MinV6),
    CONTAINS("contains", "1043", ContainsValidator.class, VersionCode.MinV6),
    PROPERTYNAMES("propertyNames", "1044", PropertyNamesValidator.class, VersionCode.MinV6),
    DEPENDENT_REQUIRED("dependentRequired", "1045", DependentRequired.class, VersionCode.MinV201909),
    DEPENDENT_SCHEMAS("dependentSchemas", "1046", DependentSchemas.class, VersionCode.MinV201909),
    UNEVALUATED_PROPERTIES("unevaluatedProperties","1047", UnEvaluatedPropertiesValidator.class, VersionCode.MinV6),
    MAX_CONTAINS("maxContains", "1048", MinMaxContainsValidator.class, VersionCode.MinV201909),
    MIN_CONTAINS("minContains", "1049", MinMaxContainsValidator.class, VersionCode.MinV201909);

    private static final Map<String, ValidatorTypeCode> CONSTANTS = new HashMap<String, ValidatorTypeCode>();

    static {
        for (ValidatorTypeCode c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;
    private final String errorCode;
    private String customMessage;
    private final String errorCodeKey;
    private final Class<?> validator;
    private final VersionCode versionCode;


    ValidatorTypeCode(String value, String errorCode, Class<?> validator, VersionCode versionCode) {
        this.value = value;
        this.errorCode = errorCode;
        this.errorCodeKey = value + "ErrorCode";
        this.validator = validator;
        this.versionCode = versionCode;
        this.customMessage = null;
    }

    public static List<ValidatorTypeCode> getNonFormatKeywords(SpecVersion.VersionFlag versionFlag) {
        final List<ValidatorTypeCode> result = new ArrayList<ValidatorTypeCode>();
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
        } else {
            return constant;
        }
    }

    public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) throws Exception {
        if (validator == null) {
            throw new UnsupportedOperationException("No suitable validator for " + getValue());
        }
        // if the config version is not match the validator
        @SuppressWarnings("unchecked")
        Constructor<JsonValidator> c = ((Class<JsonValidator>) validator).getConstructor(
                new Class[]{String.class, JsonNode.class, JsonSchema.class, ValidationContext.class});
        return c.newInstance(schemaPath + "/" + getValue(), schemaNode, parentSchema, validationContext);
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String getValue() {
        return value;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setCustomMessage(String message) {
        this.customMessage = message;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public String getErrorCodeKey() {
        return errorCodeKey;
    }

    public VersionCode getVersionCode() {
        return versionCode;
    }

    @Override
    public String getErrorCodeValue() {
        return getValue();
    }
}
