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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ValidatorTypeCode implements Keyword, ErrorMessageType {
    ADDITIONAL_PROPERTIES("additionalProperties", "1001", new MessageFormat(I18nSupport.getString("additionalProperties")), AdditionalPropertiesValidator.class, 15), // v4|v6|v7|v201909
    ALL_OF("allOf", "1002", new MessageFormat(I18nSupport.getString("allOf")), AllOfValidator.class, 15),
    ANY_OF("anyOf", "1003", new MessageFormat(I18nSupport.getString("anyOf")), AnyOfValidator.class, 15),
    CROSS_EDITS("crossEdits", "1004", new MessageFormat(I18nSupport.getString("crossEdits")), null, 15),
    DEPENDENCIES("dependencies", "1007", new MessageFormat(I18nSupport.getString("dependencies")), DependenciesValidator.class, 15),
    EDITS("edits", "1005", new MessageFormat(I18nSupport.getString("edits")), null, 15),
    ENUM("enum", "1008", new MessageFormat(I18nSupport.getString("enum")), EnumValidator.class, 15),
    FORMAT("format", "1009", new MessageFormat(I18nSupport.getString("format")), null, 15) {
        @Override
        public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext)
                throws Exception {
            throw new UnsupportedOperationException("Use FormatKeyword instead");
        }
    },
    ITEMS("items", "1010", new MessageFormat(I18nSupport.getString("items")), ItemsValidator.class, 15),
    MAXIMUM("maximum", "1011", new MessageFormat(I18nSupport.getString("maximum")), MaximumValidator.class, 15),
    MAX_ITEMS("maxItems", "1012", new MessageFormat(I18nSupport.getString("maxItems")), MaxItemsValidator.class, 15),
    MAX_LENGTH("maxLength", "1013", new MessageFormat(I18nSupport.getString("maxLength")), MaxLengthValidator.class, 15),
    MAX_PROPERTIES("maxProperties", "1014", new MessageFormat(I18nSupport.getString("maxProperties")), MaxPropertiesValidator.class, 15),
    MINIMUM("minimum", "1015", new MessageFormat(I18nSupport.getString("minimum")), MinimumValidator.class, 15),
    MIN_ITEMS("minItems", "1016", new MessageFormat(I18nSupport.getString("minItems")), MinItemsValidator.class, 15),
    MIN_LENGTH("minLength", "1017", new MessageFormat(I18nSupport.getString("minLength")), MinLengthValidator.class, 15),
    MIN_PROPERTIES("minProperties", "1018", new MessageFormat(I18nSupport.getString("minProperties")), MinPropertiesValidator.class, 15),
    MULTIPLE_OF("multipleOf", "1019", new MessageFormat(I18nSupport.getString("multipleOf")), MultipleOfValidator.class, 15),
    NOT_ALLOWED("notAllowed", "1033", new MessageFormat(I18nSupport.getString("notAllowed")), NotAllowedValidator.class, 15),
    NOT("not", "1020", new MessageFormat(I18nSupport.getString("not")), NotValidator.class, 15),
    ONE_OF("oneOf", "1022", new MessageFormat(I18nSupport.getString("oneOf")), OneOfValidator.class, 15),
    PATTERN_PROPERTIES("patternProperties", "1024", new MessageFormat(I18nSupport.getString("patternProperties")), PatternPropertiesValidator.class, 15),
    PATTERN("pattern", "1023", new MessageFormat(I18nSupport.getString("pattern")), PatternValidator.class, 15),
    PROPERTIES("properties", "1025", new MessageFormat(I18nSupport.getString("properties")), PropertiesValidator.class, 15),
    READ_ONLY("readOnly", "1032", new MessageFormat(I18nSupport.getString("readOnly")), ReadOnlyValidator.class, 15),
    REF("$ref", "1026", new MessageFormat(I18nSupport.getString("$ref")), RefValidator.class, 15),
    REQUIRED("required", "1028", new MessageFormat(I18nSupport.getString("required")), RequiredValidator.class, 15),
    TYPE("type", "1029", new MessageFormat(I18nSupport.getString("type")), TypeValidator.class, 15),
    UNION_TYPE("unionType", "1030", new MessageFormat(I18nSupport.getString("unionType")), UnionTypeValidator.class, 15),
    UNIQUE_ITEMS("uniqueItems", "1031", new MessageFormat(I18nSupport.getString("uniqueItems")), UniqueItemsValidator.class, 15),
    DATETIME("dateTime", "1034", new MessageFormat(I18nSupport.getString("dateTime")), null, 15),
    UUID("uuid", "1035", new MessageFormat(I18nSupport.getString("uuid")), null, 15),
    ID("id", "1036", new MessageFormat(I18nSupport.getString("id")), null, 15),
    IF_THEN_ELSE("if", "1037", null, IfValidator.class, 12),  // V7|V201909
    EXCLUSIVE_MAXIMUM("exclusiveMaximum", "1038", new MessageFormat(I18nSupport.getString("exclusiveMaximum")), ExclusiveMaximumValidator.class, 14),  // V6|V7|V201909
    EXCLUSIVE_MINIMUM("exclusiveMinimum", "1039", new MessageFormat(I18nSupport.getString("exclusiveMinimum")), ExclusiveMinimumValidator.class, 14),
    TRUE("true", "1040", null, TrueValidator.class, 14),
    FALSE("false", "1041", new MessageFormat(I18nSupport.getString("false")), FalseValidator.class, 14),
    CONST("const", "1042", new MessageFormat(I18nSupport.getString("const")), ConstValidator.class, 14),
    CONTAINS("contains", "1043", new MessageFormat(I18nSupport.getString("contains")), ContainsValidator.class, 14),
    PROPERTYNAMES("propertyNames", "1044", new MessageFormat(I18nSupport.getString("propertyNames")), PropertyNamesValidator.class, 14),
    DEPENDENT_REQUIRED("dependentRequired", "1045", new MessageFormat(I18nSupport.getString("dependentRequired")), DependentRequired.class, 8), // V201909
    DEPENDENT_SCHEMAS("dependentSchemas", "1046", new MessageFormat(I18nSupport.getString("dependentSchemas")), DependentSchemas.class, 8); // V201909

    private static Map<String, ValidatorTypeCode> constants = new HashMap<String, ValidatorTypeCode>();
    private static SpecVersion specVersion = new SpecVersion();

    static {
        for (ValidatorTypeCode c : values()) {
            constants.put(c.value, c);
        }
    }

    private final String value;
    private final String errorCode;
    private final MessageFormat messageFormat;
    private String customMessage;
    private final String errorCodeKey;
    private final Class validator;
    private final long versionCode;


    private ValidatorTypeCode(String value, String errorCode, MessageFormat messageFormat, Class validator, long versionCode) {
        this.value = value;
        this.errorCode = errorCode;
        this.messageFormat = messageFormat;
        this.errorCodeKey = value + "ErrorCode";
        this.validator = validator;
        this.versionCode = versionCode;
        this.customMessage = null;
    }

    public static List<ValidatorTypeCode> getNonFormatKeywords(SpecVersion.VersionFlag versionFlag) {
        final List<ValidatorTypeCode> result = new ArrayList<ValidatorTypeCode>();
        for (ValidatorTypeCode keyword : values()) {
            if (!FORMAT.equals(keyword) && specVersion.getVersionFlags(keyword.versionCode).contains(versionFlag)) {
                result.add(keyword);
            }
        }
        return result;
    }

    public static ValidatorTypeCode fromValue(String value) {
        ValidatorTypeCode constant = constants.get(value);
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
