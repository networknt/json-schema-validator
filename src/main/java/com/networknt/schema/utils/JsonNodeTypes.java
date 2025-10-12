package com.networknt.schema.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;

public class JsonNodeTypes {
    private static final long V6_VALUE = SpecificationVersion.DRAFT_6.getOrder();

    private static final String TYPE = "type";
    private static final String ENUM = "enum";
    private static final String REF = "$ref";
    private static final String NULLABLE = "nullable";

    public static boolean isNodeNullable(JsonNode schema){
        JsonNode nullable = schema.get(NULLABLE);
        return nullable != null && nullable.asBoolean();
    }

    public static boolean equalsToSchemaType(JsonNode node, JsonType schemaType, Schema parentSchema, SchemaContext schemaContext, ExecutionContext executionContext) {
        SchemaRegistryConfig config = schemaContext.getSchemaRegistryConfig();
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
            if (schemaType == JsonType.INTEGER && nodeType == JsonType.NUMBER && node.canConvertToExactIntegral() && V6_VALUE <= detectVersion(schemaContext)) {
                return true;
            }

            if (nodeType == JsonType.NULL) {
                if (parentSchema != null && schemaContext.isNullableKeywordEnabled()) {
                    Schema grandParentSchema = parentSchema.getParentSchema();
                    if (grandParentSchema != null && JsonNodeTypes.isNodeNullable(grandParentSchema.getSchemaNode())
                            || JsonNodeTypes.isNodeNullable(parentSchema.getSchemaNode())) {
                        return true;
                    }
                }
            }

            // Skip the type validation when the schema is an enum object schema. Since the current type
            // of node itself can be used for type validation.
            if (!config.isStrict("type", Boolean.TRUE) && isEnumObjectSchema(parentSchema, executionContext)) {
                return true;
            }
            if (config.isTypeLoose()) {
                // if typeLoose is true, everything can be a size 1 array
                if (schemaType == JsonType.ARRAY) {
                    return true;
                }
                if (nodeType == JsonType.STRING) {
                    if (schemaType == JsonType.INTEGER) {
                        return Strings.isInteger(node.textValue());
                    } else if (schemaType == JsonType.BOOLEAN) {
                        return Strings.isBoolean(node.textValue());
                    } else if (schemaType == JsonType.NUMBER) {
                        return Strings.isNumeric(node.textValue());
                    }
                }
            }

            return false;
        }
        return true;
    }

    private static long detectVersion(SchemaContext schemaContext) {
        return schemaContext.getDialect().getSpecificationVersion().getOrder();
    }

    /**
     * Check if the type of the JsonNode's value is number based on the
     * status of typeLoose flag.
     *
     * @param node        the JsonNode to check
     * @param config      the SchemaValidatorsConfig to depend on
     * @return boolean to indicate if it is a number
     */
    public static boolean isNumber(JsonNode node, SchemaRegistryConfig config) {
        if (node.isNumber()) {
            return true;
        } else if (config.isTypeLoose()) {
            if (TypeFactory.getValueNodeType(node, config) == JsonType.STRING) {
                return Strings.isNumeric(node.textValue());
            }
        }
        return false;
    }

    private static boolean isEnumObjectSchema(Schema jsonSchema, ExecutionContext executionContext) {
        
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
            refNode = REF.equals(executionContext.getEvaluationPath().getParent().getElement(-1));
        }
        if (typeNode != null && enumNode != null && refNode) {
            return TypeFactory.getSchemaNodeType(typeNode) == JsonType.OBJECT && enumNode.isArray();
        }
        return false;
    }
}
