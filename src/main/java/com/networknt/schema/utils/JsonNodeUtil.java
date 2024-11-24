package com.networknt.schema.utils;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonType;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.ValidationContext;

public class JsonNodeUtil {
    private static final long V6_VALUE = VersionFlag.V6.getVersionFlagValue();

    private static final String TYPE = "type";
    private static final String ENUM = "enum";
    private static final String REF = "$ref";
    private static final String NULLABLE = "nullable";

    public static Collection<String> allPaths(PathType pathType, String root, JsonNode node) {
        Collection<String> collector = new ArrayList<>();
        visitNode(pathType, root, node, collector);
        return collector;
    }

    private static void visitNode(PathType pathType, String root, JsonNode node, Collection<String> collector) {
        if (node.isObject()) {
            visitObject(pathType, root, node, collector);
        } else if (node.isArray()) {
            visitArray(pathType, root, node, collector);
        }
    }

    private static void visitArray(PathType pathType, String root, JsonNode node, Collection<String> collector) {
        int size = node.size();
        for (int i = 0; i < size; ++i) {
            String path = pathType.append(root, i);
            collector.add(path);
            visitNode(pathType, path, node.get(i), collector);
        }
    }

    private static void visitObject(PathType pathType, String root, JsonNode node, Collection<String> collector) {
        node.fields().forEachRemaining(entry -> {
            String path = pathType.append(root, entry.getKey());
            collector.add(path);
            visitNode(pathType, path, entry.getValue(), collector);
        });
    }

    public static boolean isNodeNullable(JsonNode schema){
        JsonNode nullable = schema.get(NULLABLE);
	    return nullable != null && nullable.asBoolean();
    }

    //Check to see if a JsonNode is nullable with checking the isHandleNullableField
    public static boolean isNodeNullable(JsonNode schema, SchemaValidatorsConfig config){
        // check if the parent schema declares the fields as nullable
        if (config.isNullableKeywordEnabled()) {
            return isNodeNullable(schema);
        }
        return false;
    }

    public static boolean equalsToSchemaType(JsonNode node, JsonType schemaType, JsonSchema parentSchema, ValidationContext validationContext) {
        SchemaValidatorsConfig config = validationContext.getConfig();
        JsonType nodeType = TypeFactory.getValueNodeType(node, config);
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
            if (schemaType == JsonType.INTEGER && nodeType == JsonType.NUMBER && node.canConvertToExactIntegral() && V6_VALUE <= detectVersion(validationContext)) {
                return true;
            }

            if (nodeType == JsonType.NULL) {
                if (parentSchema != null && config.isNullableKeywordEnabled()) {
                    JsonSchema grandParentSchema = parentSchema.getParentSchema();
                    if (grandParentSchema != null && JsonNodeUtil.isNodeNullable(grandParentSchema.getSchemaNode())
                            || JsonNodeUtil.isNodeNullable(parentSchema.getSchemaNode())) {
                        return true;
                    }
                }
            }

            // Skip the type validation when the schema is an enum object schema. Since the current type
            // of node itself can be used for type validation.
            if (isEnumObjectSchema(parentSchema)) {
                return true;
            }
            if (config != null && config.isTypeLoose()) {
                // if typeLoose is true, everything can be a size 1 array
                if (schemaType == JsonType.ARRAY) {
                    return true;
                }
                if (nodeType == JsonType.STRING) {
                    if (schemaType == JsonType.INTEGER) {
	                    return StringChecker.isInteger(node.textValue());
                    } else if (schemaType == JsonType.BOOLEAN) {
	                    return StringChecker.isBoolean(node.textValue());
                    } else if (schemaType == JsonType.NUMBER) {
	                    return StringChecker.isNumeric(node.textValue());
                    }
                }
            }

            return false;
        }
        return true;
    }

    private static long detectVersion(ValidationContext validationContext) {
        return validationContext.activeDialect().orElse(VersionFlag.V4).getVersionFlagValue();
    }

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
                return StringChecker.isNumeric(node.textValue());
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
        boolean refNode = false;

        if (jsonSchema != null) {
            if (jsonSchema.getSchemaNode() != null) {
                typeNode = jsonSchema.getSchemaNode().get(TYPE);
                enumNode = jsonSchema.getSchemaNode().get(ENUM);
            }
            refNode = REF.equals(jsonSchema.getEvaluationPath().getElement(-1));
        }
        if (typeNode != null && enumNode != null && refNode) {
            return TypeFactory.getSchemaNodeType(typeNode) == JsonType.OBJECT && enumNode.isArray();
        }
        return false;
    }
}
