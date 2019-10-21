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

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public enum ValidatorTypeCode implements Keyword, ErrorMessageType {
    ADDITIONAL_PROPERTIES("additionalProperties", "1001", new MessageFormat(
            "{0}.{1}: is not defined in the schema and the schema does not allow additional properties"), AdditionalPropertiesValidator.class),
    ALL_OF("allOf", "1002", new MessageFormat("{0}: should be valid to all the schemas {1}"), AllOfValidator.class),
    ANY_OF("anyOf", "1003", new MessageFormat("{0}: should be valid to any of the schemas {1}"), AnyOfValidator.class),
    CROSS_EDITS("crossEdits", "1004", new MessageFormat("{0}: has an error with 'cross edits'"), null),
    DEPENDENCIES("dependencies", "1007", new MessageFormat("{0}: has an error with dependencies {1}"), DependenciesValidator.class),
    EDITS("edits", "1005", new MessageFormat("{0}: has an error with 'edits'"), null),
    ENUM("enum", "1008", new MessageFormat("{0}: does not have a value in the enumeration {1}"), EnumValidator.class),
    FORMAT("format", "1009", new MessageFormat("{0}: does not match the {1} pattern {2}"), null){
        @Override
        public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext)
                throws Exception {
            throw new UnsupportedOperationException("Use FormatKeyword instead");
        }
    },
    ITEMS("items", "1010", new MessageFormat("{0}[{1}]: no validator found at this index"), ItemsValidator.class),
    MAXIMUM("maximum", "1011", new MessageFormat("{0}: must have a maximum value of {1}"), MaximumValidator.class),
    MAX_ITEMS("maxItems", "1012", new MessageFormat("{0}: there must be a maximum of {1} items in the array"), MaxItemsValidator.class),
    MAX_LENGTH("maxLength", "1013", new MessageFormat("{0}: may only be {1} characters long"), MaxLengthValidator.class),
    MAX_PROPERTIES("maxProperties", "1014", new MessageFormat("{0}: may only have a maximum of {1} properties"), MaxPropertiesValidator.class),
    MINIMUM("minimum", "1015", new MessageFormat("{0}: must have a minimum value of {1}"), MinimumValidator.class),
    MIN_ITEMS("minItems", "1016", new MessageFormat("{0}: there must be a minimum of {1} items in the array"), MinItemsValidator.class),
    MIN_LENGTH("minLength", "1017", new MessageFormat("{0}: must be at least {1} characters long"), MinLengthValidator.class),
    MIN_PROPERTIES("minProperties", "1018", new MessageFormat("{0}: should have a minimum of {1} properties"), MinPropertiesValidator.class),
    MULTIPLE_OF("multipleOf", "1019", new MessageFormat("{0}: must be multiple of {1}"), MultipleOfValidator.class),
    NOT_ALLOWED("notAllowed", "1033", new MessageFormat("{0}.{1}: is not allowed but it is in the data"), NotAllowedValidator.class),
    NOT("not", "1020", new MessageFormat("{0}: should not be valid to the schema {1}"), NotValidator.class),
    ONE_OF("oneOf", "1022", new MessageFormat("{0}: should be valid to one and only one of the schemas {1}"), OneOfValidator.class),
    PATTERN_PROPERTIES("patternProperties", "1024", new MessageFormat("{0}: has some error with 'pattern properties'"), PatternPropertiesValidator.class),
    PATTERN("pattern", "1023", new MessageFormat("{0}: does not match the regex pattern {1}"), PatternValidator.class),
    PROPERTIES("properties", "1025", new MessageFormat("{0}: has an error with 'properties'"), PropertiesValidator.class),
    READ_ONLY("readOnly", "1032", new MessageFormat("{0}: is a readonly field, it cannot be changed"), ReadOnlyValidator.class),
    REF("$ref", "1026", new MessageFormat("{0}: has an error with 'refs'"), RefValidator.class),
    REQUIRED("required", "1028", new MessageFormat("{0}.{1}: is missing but it is required"), RequiredValidator.class),
    TYPE("type", "1029", new MessageFormat("{0}: {1} found, {2} expected"), TypeValidator.class),
    UNION_TYPE("unionType", "1030", new MessageFormat("{0}: {1} found, but {2} is required"), UnionTypeValidator.class),
    UNIQUE_ITEMS("uniqueItems", "1031", new MessageFormat("{0}: the items in the array must be unique"), UniqueItemsValidator.class),
    DATETIME("date-time", "1034", new MessageFormat("{0}: {1} is an invalid {2}"), null),
    UUID("uuid", "1035", new MessageFormat("{0}: {1} is an invalid {2}"), null),
    ID("id", "1036", new MessageFormat("{0}: {1} is an invalid segment for URI {2}"), null);

    private static Map<String, ValidatorTypeCode> constants = new HashMap<String, ValidatorTypeCode>();

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

    private ValidatorTypeCode(String value, String errorCode, MessageFormat messageFormat, Class validator) {
        this.value = value;
        this.errorCode = errorCode;
        this.messageFormat = messageFormat;
        this.errorCodeKey = value + "ErrorCode";
        this.validator = validator;
    }

    public static List<ValidatorTypeCode> getNonFormatKeywords() {
        final List<ValidatorTypeCode> result = new ArrayList<ValidatorTypeCode>();
        for (ValidatorTypeCode keyword: values()) {
            if (!FORMAT.equals(keyword)) {
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
        @SuppressWarnings("unchecked")
        Constructor<JsonValidator> c = ((Class<JsonValidator>) validator).getConstructor(
                new Class[] { String.class, JsonNode.class, JsonSchema.class, ValidationContext.class });
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

}
