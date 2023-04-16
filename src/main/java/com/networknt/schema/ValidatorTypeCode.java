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

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

enum VersionCode {
    AllVersions(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V4, SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV6(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV7(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MaxV201909(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V4, SpecVersion.VersionFlag.V6, SpecVersion.VersionFlag.V7, SpecVersion.VersionFlag.V201909 }),
    MinV201909(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V201909, SpecVersion.VersionFlag.V202012 }),
    MinV202012(new SpecVersion.VersionFlag[] { SpecVersion.VersionFlag.V202012 });

    private static final SpecVersion specVersion = new SpecVersion();

    private final SpecVersion.VersionFlag[] versionFlags;

    VersionCode(SpecVersion.VersionFlag[] versionFlags) {
        this.versionFlags = versionFlags;
    }
    long getValue() {
        return specVersion.getVersionValue(new HashSet<>(Arrays.asList(this.versionFlags)));
    }
}

public enum ValidatorTypeCode implements Keyword, ErrorMessageType {
    ADDITIONAL_PROPERTIES("additionalProperties", "1001", new MessageFormat(I18nSupport.getString("additionalProperties")), AdditionalPropertiesValidator.class, VersionCode.AllVersions),
    ALL_OF("allOf", "1002", new MessageFormat(I18nSupport.getString("allOf")), AllOfValidator.class, VersionCode.AllVersions),
    ANY_OF("anyOf", "1003", new MessageFormat(I18nSupport.getString("anyOf")), AnyOfValidator.class, VersionCode.AllVersions),
    CROSS_EDITS("crossEdits", "1004", new MessageFormat(I18nSupport.getString("crossEdits")), null, VersionCode.AllVersions),
    DEPENDENCIES("dependencies", "1007", new MessageFormat(I18nSupport.getString("dependencies")), DependenciesValidator.class, VersionCode.AllVersions),
    EDITS("edits", "1005", new MessageFormat(I18nSupport.getString("edits")), null, VersionCode.AllVersions),
    ENUM("enum", "1008", new MessageFormat(I18nSupport.getString("enum")), EnumValidator.class, VersionCode.AllVersions),
    FORMAT("format", "1009", new MessageFormat(I18nSupport.getString("format")), null, VersionCode.AllVersions) {
        @Override
        public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext)
                throws Exception {
            throw new UnsupportedOperationException("Use FormatKeyword instead");
        }
    },
    ITEMS("items", "1010", new MessageFormat(I18nSupport.getString("items")), ItemsValidator.class, VersionCode.MaxV201909),
    ITEMS_202012("items", "1010", new MessageFormat(I18nSupport.getString("items")), ItemsValidator202012.class, VersionCode.MinV202012),
    MAXIMUM("maximum", "1011", new MessageFormat(I18nSupport.getString("maximum")), MaximumValidator.class, VersionCode.AllVersions),
    MAX_ITEMS("maxItems", "1012", new MessageFormat(I18nSupport.getString("maxItems")), MaxItemsValidator.class, VersionCode.AllVersions),
    MAX_LENGTH("maxLength", "1013", new MessageFormat(I18nSupport.getString("maxLength")), MaxLengthValidator.class, VersionCode.AllVersions),
    MAX_PROPERTIES("maxProperties", "1014", new MessageFormat(I18nSupport.getString("maxProperties")), MaxPropertiesValidator.class, VersionCode.AllVersions),
    MINIMUM("minimum", "1015", new MessageFormat(I18nSupport.getString("minimum")), MinimumValidator.class, VersionCode.AllVersions),
    MIN_ITEMS("minItems", "1016", new MessageFormat(I18nSupport.getString("minItems")), MinItemsValidator.class, VersionCode.AllVersions),
    MIN_LENGTH("minLength", "1017", new MessageFormat(I18nSupport.getString("minLength")), MinLengthValidator.class, VersionCode.AllVersions),
    MIN_PROPERTIES("minProperties", "1018", new MessageFormat(I18nSupport.getString("minProperties")), MinPropertiesValidator.class, VersionCode.AllVersions),
    MULTIPLE_OF("multipleOf", "1019", new MessageFormat(I18nSupport.getString("multipleOf")), MultipleOfValidator.class, VersionCode.AllVersions),
    NOT_ALLOWED("notAllowed", "1033", new MessageFormat(I18nSupport.getString("notAllowed")), NotAllowedValidator.class, VersionCode.AllVersions),
    NOT("not", "1020", new MessageFormat(I18nSupport.getString("not")), NotValidator.class, VersionCode.AllVersions),
    ONE_OF("oneOf", "1022", new MessageFormat(I18nSupport.getString("oneOf")), OneOfValidator.class, VersionCode.AllVersions),
    PATTERN_PROPERTIES("patternProperties", "1024", new MessageFormat(I18nSupport.getString("patternProperties")), PatternPropertiesValidator.class, VersionCode.AllVersions),
    PATTERN("pattern", "1023", new MessageFormat(I18nSupport.getString("pattern")), PatternValidator.class, VersionCode.AllVersions),
    PREFIX_ITEMS("prefixItems", "1048", new MessageFormat(I18nSupport.getString("prefixItems")), PrefixItemsValidator.class, VersionCode.MinV202012),
    PROPERTIES("properties", "1025", new MessageFormat(I18nSupport.getString("properties")), PropertiesValidator.class, VersionCode.AllVersions),
    READ_ONLY("readOnly", "1032", new MessageFormat(I18nSupport.getString("readOnly")), ReadOnlyValidator.class, VersionCode.AllVersions),
    REF("$ref", "1026", new MessageFormat(I18nSupport.getString("$ref")), RefValidator.class, VersionCode.AllVersions),
    REQUIRED("required", "1028", new MessageFormat(I18nSupport.getString("required")), RequiredValidator.class, VersionCode.AllVersions),
    TYPE("type", "1029", new MessageFormat(I18nSupport.getString("type")), TypeValidator.class, VersionCode.AllVersions),
    UNION_TYPE("unionType", "1030", new MessageFormat(I18nSupport.getString("unionType")), UnionTypeValidator.class, VersionCode.AllVersions),
    UNIQUE_ITEMS("uniqueItems", "1031", new MessageFormat(I18nSupport.getString("uniqueItems")), UniqueItemsValidator.class, VersionCode.AllVersions),
    DATETIME("dateTime", "1034", new MessageFormat(I18nSupport.getString("dateTime")), null, VersionCode.AllVersions),
    UUID("uuid", "1035", new MessageFormat(I18nSupport.getString("uuid")), null, VersionCode.AllVersions),
    ID("id", "1036", new MessageFormat(I18nSupport.getString("id")), null, VersionCode.AllVersions),
    IF_THEN_ELSE("if", "1037", null, IfValidator.class, VersionCode.MinV7),
    EXCLUSIVE_MAXIMUM("exclusiveMaximum", "1038", new MessageFormat(I18nSupport.getString("exclusiveMaximum")), ExclusiveMaximumValidator.class, VersionCode.MinV6),
    EXCLUSIVE_MINIMUM("exclusiveMinimum", "1039", new MessageFormat(I18nSupport.getString("exclusiveMinimum")), ExclusiveMinimumValidator.class, VersionCode.MinV6),
    TRUE("true", "1040", null, TrueValidator.class, VersionCode.MinV6),
    FALSE("false", "1041", new MessageFormat(I18nSupport.getString("false")), FalseValidator.class, VersionCode.MinV6),
    CONST("const", "1042", new MessageFormat(I18nSupport.getString("const")), ConstValidator.class, VersionCode.MinV6),
    CONTAINS("contains", "1043", new MessageFormat(I18nSupport.getString("contains")), ContainsValidator.class, VersionCode.MinV6),
    PROPERTYNAMES("propertyNames", "1044", new MessageFormat(I18nSupport.getString("propertyNames")), PropertyNamesValidator.class, VersionCode.MinV6),
    DEPENDENT_REQUIRED("dependentRequired", "1045", new MessageFormat(I18nSupport.getString("dependentRequired")), DependentRequired.class, VersionCode.MinV201909),
    DEPENDENT_SCHEMAS("dependentSchemas", "1046", new MessageFormat(I18nSupport.getString("dependentSchemas")), DependentSchemas.class, VersionCode.MinV201909),
    UNEVALUATED_PROPERTIES("unevaluatedProperties","1047",new MessageFormat(I18nSupport.getString("unevaluatedProperties")),UnEvaluatedPropertiesValidator.class, VersionCode.MinV6),
    MAX_CONTAINS("maxContains", "1048", new MessageFormat(I18nSupport.getString("maxContains")), MinMaxContainsValidator.class, VersionCode.MinV201909),
    MIN_CONTAINS("minContains", "1049", new MessageFormat(I18nSupport.getString("minContains")), MinMaxContainsValidator.class, VersionCode.MinV201909);

    private static final Map<String, ValidatorTypeCode> CONSTANTS = new HashMap<String, ValidatorTypeCode>();
    private static final SpecVersion SPEC_VERSION = new SpecVersion();

    static {
        for (ValidatorTypeCode c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;
    private final String errorCode;
    private final MessageFormat messageFormat;
    private String customMessage;
    private final String errorCodeKey;
    private final Class<?> validator;
    private final long versionCode;


    private ValidatorTypeCode(String value, String errorCode, MessageFormat messageFormat, Class<?> validator, VersionCode versionCode) {
        this.value = value;
        this.errorCode = errorCode;
        this.messageFormat = messageFormat;
        this.errorCodeKey = value + "ErrorCode";
        this.validator = validator;
        this.versionCode = versionCode.getValue();
        this.customMessage = null;
    }

    public static List<ValidatorTypeCode> getNonFormatKeywords(SpecVersion.VersionFlag versionFlag) {
        final List<ValidatorTypeCode> result = new ArrayList<ValidatorTypeCode>();
        for (ValidatorTypeCode keyword : values()) {
            if (!FORMAT.equals(keyword) && SPEC_VERSION.getVersionFlags(keyword.versionCode).contains(versionFlag)) {
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

    public MessageFormat getMessageFormat() {
        return messageFormat;
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

    public long getVersionCode() {
        return versionCode;
    }
}
