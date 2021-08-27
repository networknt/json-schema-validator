package com.networknt.schema.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.schema.*;

import java.util.Iterator;

public class JsonNodeUtil {
    private static final String TYPE = "type";
    private static final String ENUM = "enum";
    private static final String REF = "$ref";
    private static final String NULLABLE = "nullable";

    public static boolean isNodeNullable(JsonNode schema){
        JsonNode nullable = schema.get(NULLABLE);
        if (nullable != null && nullable.asBoolean()) {
            return true;
        }
        return false;
    }

    //Check to see if a JsonNode is nullable with checking the isHandleNullableField
    public static boolean isNodeNullable(JsonNode schema, SchemaValidatorsConfig config){
        // check if the parent schema declares the fields as nullable
        if (config.isHandleNullableField()) {
            return isNodeNullable(schema);
        }
        return false;
    }

    //Check to see if any child node for the OneOf SchemaNode is nullable
    public static boolean isChildNodeNullable(ArrayNode oneOfSchemaNode,SchemaValidatorsConfig config){
        Iterator iterator = oneOfSchemaNode.elements();
        while(iterator.hasNext()){
            //If one of the child Node for oneOf is nullable, it means the whole oneOf is nullable
            if (isNodeNullable((JsonNode)iterator.next(),config)) return true;
        }
        return false;
    }

    public static boolean matchOneOfTypeNode(JsonNode oneOfSchemaNode, JsonType nodeType  ){
        Iterator iterator = oneOfSchemaNode.elements();
        while (iterator.hasNext()){
            JsonNode oneOfTypeNode = (JsonNode) iterator.next();
            JsonNode typeTextNode = oneOfTypeNode.get(TYPE);
            if(typeTextNode != null && typeTextNode.asText().equals(nodeType.toString())) //If the nodeType is oneOf the type defined in the oneOf , return true
                return true;
        }
        return false;
    }

    public static boolean equalsToSchemaType(JsonNode node, JsonSchema schema, SchemaValidatorsConfig config) {
        // in the case that node type is not the same as schema type, try to convert node to the
        // same type of schema. In REST API, query parameters, path parameters and headers are all
        // string type and we must convert, otherwise, all schema validations will fail.
        JsonType schemaType = getSchemaJsonType(schema);
        return equalsToSchemaType(node,schemaType,schema.getParentSchema(),config);

    }

    public static JsonType getSchemaJsonType(JsonSchema schema){
        JsonNode typeNode = schema.getSchemaNode().get(TYPE);
        if(typeNode!= null) return JsonType.valueOf(typeNode.asText().toUpperCase());
        return JsonType.UNKNOWN;
    }



    public static boolean equalsToSchemaType(JsonNode node, JsonType schemaType, JsonSchema parentSchema, SchemaValidatorsConfig config) {
        // in the case that node type is not the same as schema type, try to convert node to the
        // same type of schema. In REST API, query parameters, path parameters and headers are all
        // string type and we must convert, otherwise, all schema validations will fail.

        JsonType nodeType = TypeFactory.getValueNodeType(node, config);

        if (nodeType != schemaType) {
            if (schemaType == JsonType.ANY) {
                return true;
            }

            if (schemaType == JsonType.NUMBER && nodeType == JsonType.INTEGER) {
                return true;
            }

            ValidatorState state = (ValidatorState) CollectorContext.getInstance().get(ValidatorState.VALIDATOR_STATE_KEY);
            if(JsonType.NULL.equals(nodeType) ){
                if(state.isComplexValidator() && JsonNodeUtil.isNodeNullable(parentSchema.getParentSchema().getSchemaNode(), config) || JsonNodeUtil.isNodeNullable(parentSchema.getSchemaNode()) ){
                    return true;
                }
            }

            // Skip the type validation when the schema is an enum object schema. Since the current type
            // of node itself can be used for type validation.
            if (isEnumObjectSchema(parentSchema)) {
                return true;
            }
            if (config.isTypeLoose()) {
                // if typeLoose is true, everything can be a size 1 array
                if (schemaType == JsonType.ARRAY) {
                    return true;
                }
                if (nodeType == JsonType.STRING) {
                    if (schemaType == JsonType.INTEGER) {
                        if (isInteger(node.textValue())) {
                            return true;
                        }
                    } else if (schemaType == JsonType.BOOLEAN) {
                        if (isBoolean(node.textValue())) {
                            return true;
                        }
                    } else if (schemaType == JsonType.NUMBER) {
                        if (isNumeric(node.textValue())) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
        return true;
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
            if (i < len) {
                c = str.charAt(i++);
                if (c != DOT && c != CHAR_E && c != CHAR_e) {
                    return false;
                }
            }
        } else if (CHAR_1 <= c && c <= CHAR_9) {
            while (i < len && CHAR_0 <= c && c <= CHAR_9) {
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
            while (i < len && CHAR_0 <= c && c <= CHAR_9) {
                c = str.charAt(i++);
            }
        }

        if (c == CHAR_E || c == CHAR_e) {
            if (i >= len) {
                return false;
            }
            c = str.charAt(i++);
            if (c == PLUS || c == MINUS) {
                if (i >= len) {
                    return false;
                }
                c = str.charAt(i++);
            }
            while (i < len && CHAR_0 <= c && c <= CHAR_9) {
                c = str.charAt(i++);
            }
        }

        return i >= len && (CHAR_0 <= c && c <= CHAR_9);
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
     *
     * @param node        the JsonNode to check
     * @param config      the SchemaValidatorsConfig to depend on
     * @return boolean to indicate if it is a number
     */
    public static boolean isNumber(JsonNode node, SchemaValidatorsConfig config) {
        if (node.isNumber()) {
            return true;
        } else if (config.isTypeLoose()) {
            if (TypeFactory.getValueNodeType(node, config) == JsonType.STRING) {
                return isNumeric(node.textValue());
            }
        }
        return false;
    }

    private static boolean isEnumObjectSchema(JsonSchema jsonSchema) {
        // There are three conditions for enum object schema
        // 1. The current schema contains key "type", and the value is object
        // 2. The current schema contains key "enum", and the value is an array
        // 3. The parent schema if refer from components, which means the corresponding enum object class would be generated
        JsonNode typeNode = null;
        JsonNode enumNode = null;
        JsonNode refNode = null;

        if (jsonSchema != null) {
            if (jsonSchema.getSchemaNode() != null) {
                typeNode = jsonSchema.getSchemaNode().get(TYPE);
                enumNode = jsonSchema.getSchemaNode().get(ENUM);
            }
            if (jsonSchema.getParentSchema() != null && jsonSchema.getParentSchema().getSchemaNode() != null) {
                refNode = jsonSchema.getParentSchema().getSchemaNode().get(REF);
            }
        }
        if (typeNode != null && enumNode != null && refNode != null) {
            return TypeFactory.getSchemaNodeType(typeNode) == JsonType.OBJECT && enumNode.isArray();
        }
        return false;
    }
}
