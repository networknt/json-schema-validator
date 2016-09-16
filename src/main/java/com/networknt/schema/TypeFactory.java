package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;

public class TypeFactory {
    public static JsonType getSchemaNodeType(JsonNode node) {
        //Single Type Definition
        if (node.isTextual()) {
            String type = node.textValue();
            if ("object".equals(type)) {
                return JsonType.OBJECT;
            }
            if ("array".equals(type)) {
                return JsonType.ARRAY;
            }
            if ("string".equals(type)) {
                return JsonType.STRING;
            }
            if ("number".equals(type)) {
                return JsonType.NUMBER;
            }
            if ("integer".equals(type)) {
                return JsonType.INTEGER;
            }
            if ("boolean".equals(type)) {
                return JsonType.BOOLEAN;
            }
            if ("any".equals(type)) {
                return JsonType.ANY;
            }
            if ("null".equals(type)) {
                return JsonType.NULL;
            }
        }

        //Union Type Definition
        if (node.isArray()) {
            return JsonType.UNION;
        }

        return JsonType.UNKNOWN;
    }

    public static JsonType getValueNodeType(JsonNode node) {
        if (node.isContainerNode()) {
            if (node.isObject())
                return JsonType.OBJECT;
            if (node.isArray())
                return JsonType.ARRAY;
            return JsonType.UNKNOWN;
        }

        if (node.isValueNode()) {
            if (node.isTextual())
                return JsonType.STRING;
            if (node.isIntegralNumber())
                return JsonType.INTEGER;
            if (node.isNumber())
                return JsonType.NUMBER;
            if (node.isBoolean())
                return JsonType.BOOLEAN;
            if (node.isNull())
                return JsonType.NULL;
            return JsonType.UNKNOWN;
        }

        return JsonType.UNKNOWN;
    }

}
