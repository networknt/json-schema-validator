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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

public class TypeValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(TypeValidator.class);

    private JsonType schemaType;
    private UnionTypeValidator unionTypeValidator;

    public TypeValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.TYPE, validationContext);
        schemaType = TypeFactory.getSchemaNodeType(schemaNode);

        if (schemaType == JsonType.UNION) {
            unionTypeValidator = new UnionTypeValidator(schemaPath, schemaNode, parentSchema, validationContext);
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public JsonType getSchemaType() {
        return schemaType;
    }

    public boolean equalsToSchemaType(JsonNode node) {
        JsonType nodeType = TypeFactory.getValueNodeType(node);
        // in the case that node type is not the same as schema type, try to convert node to the
        // same type of schema. In REST API, query parameters, path parameters and headers are all
        // string type and we must convert, otherwise, all schema validations will fail.
        if (nodeType != schemaType) {
            if (schemaType == JsonType.ANY) {
                return true;
            }
            if (schemaType == JsonType.NUMBER && nodeType == JsonType.INTEGER) {
                return true;
            }
            if (nodeType == JsonType.NULL) {
                JsonNode nullable = this.getParentSchema().getSchemaNode().get("nullable");
                if (nullable != null && nullable.asBoolean()) {
                    return true;
                }
            }
            if(config.isTypeLoose()) {
                if (nodeType == JsonType.STRING) {
                    if(schemaType == JsonType.INTEGER) {
                        if(isInteger(node.textValue())) {
                            return true;
                        }
                    } else if(schemaType == JsonType.BOOLEAN) {
                        if(isBoolean(node.textValue())) {
                            return true;
                        }
                    } else if(schemaType == JsonType.NUMBER) {
                        if(isNumeric(node.textValue())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        return true;
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        if (schemaType == JsonType.UNION) {
            return unionTypeValidator.validate(node, rootNode, at);
        }

        if (!equalsToSchemaType(node)) {
            JsonType nodeType = TypeFactory.getValueNodeType(node);
            return Collections.singleton(buildValidationMessage(at, nodeType.toString(), schemaType.toString()));
        }
        return Collections.emptySet();
    }

    public static boolean isInteger(String str) {
        if (str == null || str.equals("")) {
            return false;
        }

        // all code below could be replaced with
        //return str.matrch("[-+]?(?:0|[1-9]\\d*)")
        int i = 0;
        if (str.charAt(0) == '-' || str.charAt(0) == '+') {
            if (str.length() == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isBoolean(String s) {
        return "true".equals(s) || "false".equals(s);
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.equals("")) {
            return false;
        }

        // all code below could be replaced with
        //return str.matrch("[-+]?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")
        int i = 0;
        int len = str.length();

        if (str.charAt(i) == MINUS || str.charAt(i) == PLUS) {
            if (str.length() == 1) {
                return false;
            }
            i = 1;
        }

        char c = str.charAt(i++);

        if (c == CHAR_0) {
            // TODO: if leading zeros are supported (counter to JSON spec) handle it here
            if (i < len){
                c = str.charAt(i++);
                if (c != DOT && c != CHAR_E && c != CHAR_e) {
                    return false;
                }
            }
        } else if (CHAR_1 <= c && c <= CHAR_9) {
            while ( i < len && CHAR_0 <= c  && c <= CHAR_9 ) {
                c = str.charAt(i++);
            }
        } else {
            return false;
        }

        if (c == DOT) {
            if (i >= len) {
                return false;
            }
            c = str.charAt(i++);
            while ( i < len && CHAR_0 <= c  && c <= CHAR_9 ) {
                c = str.charAt(i++);
            }
        }

        if (c == CHAR_E || c == CHAR_e) {
            if (i >= len ) {
                return false;
            }
            c = str.charAt(i++);
            if (c == PLUS || c == MINUS) {
                if (i >= len) {
                    return false;
                }
                c = str.charAt(i++);
            }
            while ( i < len && CHAR_0 <= c  && c <= CHAR_9 ) {
                c = str.charAt(i++);
            }
        }

        return i >= len && (CHAR_0 <= c  && c <= CHAR_9);
    }

    private static final char CHAR_0 = '0';
    private static final char CHAR_1 = '1';
    private static final char CHAR_9 = '9';
    private static final char MINUS = '-';
    private static final char PLUS = '+';
    private static final char DOT = '.';
    private static final char CHAR_E = 'E';
    private static final char CHAR_e = 'e';

    /**
     * Check if the type of the JsonNode's value is number based on the
     * status of typeLoose flag.
     * @param node the JsonNode to check
     * @param isTypeLoose The flag to show whether typeLoose is enabled
     * @return boolean to indicate if it is a number
     */
    public static boolean isNumber(JsonNode node, boolean isTypeLoose) {
        if (node.isNumber()) {
            return true;
        } else if (isTypeLoose) {
            if (TypeFactory.getValueNodeType(node) == JsonType.STRING) {
                return isNumeric(node.textValue());
            }
        }
        return false;
    }
}
