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
import java.util.*;

public enum ValidatorTypeCode implements Keyword, ErrorMessageType {
    ADDITIONAL_PROPERTIES("additionalProperties", "1001", AdditionalPropertiesValidator.class, 15), // v4|v6|v7|v201909
    ALL_OF("allOf", "1002", AllOfValidator.class, 15),
    ANY_OF("anyOf", "1003",  AnyOfValidator.class, 15),
    CROSS_EDITS("crossEdits", "1004",  null, 15),
    DEPENDENCIES("dependencies", "1007", DependenciesValidator.class, 15),
    EDITS("edits", "1005", null, 15),
    ENUM("enum", "1008",  EnumValidator.class, 15),
    FORMAT("format", "1009", null, 15) {
        @Override
        public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext)
                throws Exception {
            throw new UnsupportedOperationException("Use FormatKeyword instead");
        }
    },
    ITEMS("items","1010", ItemsValidator.class, 15),
    MAXIMUM("maximum","1011", MaximumValidator.class, 15),
    MAX_ITEMS("maxItems","1012", MaxItemsValidator.class, 15),
    MAX_LENGTH("maxLength","1013", MaxLengthValidator.class, 15),
    MAX_PROPERTIES("maxProperties","1014", MaxPropertiesValidator.class, 15),
    MINIMUM("minimum","1015", MinimumValidator.class, 15),
    MIN_ITEMS("minItems","1016", MinItemsValidator.class, 15),
    MIN_LENGTH("minLength","1017", MinLengthValidator.class, 15),
    MIN_PROPERTIES("minProperties","1018", MinPropertiesValidator.class, 15),
    MULTIPLE_OF("multipleOf","1019", MultipleOfValidator.class, 15),
    NOT_ALLOWED("notAllowed","1033", NotAllowedValidator.class, 15),
    NOT("not","1020", NotValidator.class, 15),
    ONE_OF("oneOf","1022", OneOfValidator.class, 15),
    PATTERN_PROPERTIES("patternProperties","1024", PatternPropertiesValidator.class, 15),
    PATTERN("pattern","1023", PatternValidator.class, 15),
    PROPERTIES("properties","1025", PropertiesValidator.class, 15),
    READ_ONLY("readOnly","1032", ReadOnlyValidator.class, 15),
    REF("$ref","1026", RefValidator.class, 15),
    REQUIRED("required","1028", RequiredValidator.class, 15),
    TYPE("type","1029", TypeValidator.class, 15),
    UNION_TYPE("unionType","1030", UnionTypeValidator.class, 15),
    UNIQUE_ITEMS("uniqueItems","1031", UniqueItemsValidator.class, 15),
    DATETIME("date-time","1034", null, 15),
    UUID("uuid","1035", null, 15),
    ID("id","1036", null, 15),
    IF_THEN_ELSE("if", "1037", IfValidator.class, 12),  // V7|V201909
    EXCLUSIVE_MAXIMUM("exclusiveMaximum","1038", ExclusiveMaximumValidator.class, 14),  // V6|V7|V201909
    EXCLUSIVE_MINIMUM("exclusiveMinimum","1039", ExclusiveMinimumValidator.class, 14),
    TRUE("true", "1040", TrueValidator.class, 14),
    FALSE("false","1041", FalseValidator.class, 14),
    CONST("const","1042", ConstValidator.class, 14),
    CONTAINS("contains","1043", ContainsValidator.class, 14),
    PROPERTYNAMES("propertyNames","1044", PropertyNamesValidator.class, 14);

    private static Map<String, ValidatorTypeCode> constants = new HashMap<String, ValidatorTypeCode>();
    private static SpecVersion specVersion = new SpecVersion();
    private final ResourceBundle messages = ResourceBundle.getBundle("com.networknt.schema.messages");

    static {
        for (ValidatorTypeCode c : values()) {
            constants.put(c.value, c);
        }
    }

    private final String value;
    private final String errorCode;
    private final MessageFormat messageFormat;
    private final String errorCodeKey;
    private final Class validator;
    private final long versionCode;


    private ValidatorTypeCode(String value, String errorCode,  Class validator, long versionCode) {
        this.value = value;
        this.errorCode = errorCode;
        this.messageFormat = new MessageFormat(messages.getString(value));
        this.errorCodeKey = value + "ErrorCode";
        this.validator = validator;
        this.versionCode = versionCode;
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

    public String getErrorCodeKey() {
        return errorCodeKey;
    }

    public long getVersionCode() {
        return versionCode;
    }
}
