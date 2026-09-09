package com.networknt.schema.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

    /**
     * Keywords whose sub-schemas describe the same value as their parent,
     * unlike {@code properties}/{@code items}/{@code additionalProperties}.
     * {@code not} is excluded since it negates rather than describes.
     */
    private static final Set<String> COMPOSING_KEYWORDS = new HashSet<>(
            Arrays.asList("allOf", "oneOf", "anyOf"));

    public static boolean isNodeNullable(JsonNode schema) {
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
            if (schemaType == JsonType.NUMBER && nodeType == JsonType.INTEGER) {
                return true;
            }
            if (schemaType == JsonType.INTEGER && nodeType == JsonType.NUMBER && node.canConvertToExactIntegral() && V6_VALUE <= detectVersion(schemaContext)) {
                return true;
            }

            if (nodeType == JsonType.NULL) {
                if (parentSchema != null && schemaContext.isNullableKeywordEnabled()
                        && isNullableAncestor(parentSchema, executionContext)) {
                    return true;
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
            if (schemaType == JsonType.ANY) {
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Determines if the schema owning the failing {@code type} keyword is
     * nullable, walking up through composing keywords and {@code $ref} hops
     * that describe the same value, in either order and to any depth.
     *
     * @param schema the schema to start the walk from
     * @param executionContext the execution context
     * @return true if a nullable schema is found
     */
    public static boolean isNullableAncestor(Schema schema, ExecutionContext executionContext) {
        Schema current = schema;
        boolean isRefSchema = false;
        while (current != null) {
            if (!isRefSchema && isNodeNullable(current.getSchemaNode())) {
                return true;
            }
            Schema parentSchema = current.getParentSchema();
            if (parentSchema != null && isComposingKeyword(current, parentSchema)) {
                if (isNodeNullable(parentSchema.getSchemaNode())) {
                    return true;
                }
                current = parentSchema;
                isRefSchema = false;
                continue;
            }
            current = findReferencingSchema(current, executionContext);
            isRefSchema = true;
        }
        return false;
    }

    /**
     * Determines if the given schema was reached from its lexical parent via a
     * {@link #COMPOSING_KEYWORDS composing keyword}, by checking whether the
     * schema's node is one of the branches of a composing keyword on the parent.
     * This is based on the schema node identity rather than the schema location,
     * since the schema location can be rewritten by a dialect's {@code id}
     * keyword and no longer reflect the lexical path, and a property literally
     * named after a composing keyword (e.g. {@code allOf}) can otherwise be
     * mistaken for one.
     *
     * @param schema the schema to check
     * @param parentSchema the lexical parent of the schema
     * @return true if the schema was reached via a composing keyword
     */
    private static boolean isComposingKeyword(Schema schema, Schema parentSchema) {
        JsonNode schemaNode = schema.getSchemaNode();
        JsonNode parentNode = parentSchema.getSchemaNode();
        for (String keyword : COMPOSING_KEYWORDS) {
            JsonNode branches = parentNode.get(keyword);
            if (branches != null && branches.isArray()) {
                for (JsonNode branch : branches) {
                    if (branch == schemaNode) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds the schema that referenced the given schema through {@code $ref}, if
     * any, by looking at the schema evaluated immediately before it on the
     * dynamic evaluation stack.
     *
     * @param schema the schema that may have been reached through {@code $ref}
     * @param executionContext the execution context
     * @return the referencing schema, or null if none is found
     */
    private static Schema findReferencingSchema(Schema schema, ExecutionContext executionContext) {
        Iterator<Schema> ancestors = executionContext.getEvaluationSchema().descendingIterator();
        while (ancestors.hasNext()) {
            if (ancestors.next() == schema) {
                if (ancestors.hasNext()) {
                    Schema candidate = ancestors.next();
                    if (candidate.getSchemaNode().get(REF) != null) {
                        return candidate;
                    }
                }
                return null;
            }
        }
        return null;
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
