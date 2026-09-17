package com.networknt.schema.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import tools.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.keyword.KeywordType;

public class JsonNodeTypes {
    private static final long V6_VALUE = SpecificationVersion.DRAFT_6.getOrder();
    private static final long DRAFT_2019_09_VALUE = SpecificationVersion.DRAFT_2019_09.getOrder();

    private static final String TYPE = "type";
    private static final String ENUM = "enum";
    private static final String REF = "$ref";
    private static final String NULLABLE = "nullable";

    /**
     * Keywords whose sub-schemas describe the same value as the schema that
     * declares them, unlike {@code properties}/{@code items}/
     * {@code additionalProperties}, which describe a value nested inside it.
     * {@code not} is excluded since it negates rather than describes.
     */
    private static final Set<String> VALUE_PRESERVING_KEYWORDS = new HashSet<>(Arrays.asList(
            KeywordType.ALL_OF.getValue(), KeywordType.ANY_OF.getValue(), KeywordType.ONE_OF.getValue(),
            KeywordType.IF_THEN_ELSE.getValue()));

    /**
     * Keywords that reach another schema by reference. The referencing schema
     * describes the same value, though in dialects that drop members declared
     * alongside a reference its own {@code nullable} does not count.
     */
    private static final Set<String> REFERENCING_KEYWORDS = new HashSet<>(Arrays.asList(
            KeywordType.REF.getValue(), KeywordType.DYNAMIC_REF.getValue(),
            KeywordType.RECURSIVE_REF.getValue()));

    public static boolean isNodeNullable(JsonNode schema){
        JsonNode nullable = schema.get(NULLABLE);
        // asBoolean() without a default throws on a node it cannot coerce, and
        // the ancestor walk reads this member on every composing ancestor, so a
        // malformed nullable anywhere in the chain would abort the validation.
	    return nullable != null && nullable.asBoolean(false);
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
                        && isNullableAncestor(executionContext)) {
                    return true;
                }
            }

            // Skip the type validation when the schema is an enum object schema. Since the current type
            // of node itself can be used for type validation.
            if (!config.isStrict("type", Boolean.TRUE) && isEnumObjectSchema(parentSchema, executionContext)) {
                return true;
            }
            if (config != null && config.isTypeLoose()) {
                // if typeLoose is true, everything can be a size 1 array
                if (schemaType == JsonType.ARRAY) {
                    return true;
                }
                if (nodeType == JsonType.STRING) {
                    if (schemaType == JsonType.INTEGER) {
	                    return Strings.isInteger(node.asString());
                    } else if (schemaType == JsonType.BOOLEAN) {
	                    return Strings.isBoolean(node.asString());
                    } else if (schemaType == JsonType.NUMBER) {
	                    return Strings.isNumeric(node.asString());
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
     * Determines whether the value currently being validated is declared
     * nullable, either by the schema owning the executing keyword or by an
     * ancestor that describes the same value.
     * <p>
     * The walk descends the evaluation stacks, which record both the schemas
     * entered and the keyword each one was entered by, and stops at the first
     * keyword that moves to a different value such as {@code properties} or
     * {@code items}. It must therefore be called while a validation is in
     * flight; outside one the stacks are empty and the result is false.
     *
     * @param executionContext the execution context of the in-flight validation
     * @return true if the value being validated is declared nullable
     */
    public static boolean isNullableAncestor(ExecutionContext executionContext) {
        Iterator<Schema> schemas = executionContext.getEvaluationSchema().descendingIterator();
        Iterator<Object> keywords = executionContext.getEvaluationSchemaPath().descendingIterator();
        // The top of the keyword stack is the keyword executing within the top
        // schema. What the walk needs is the keyword each schema was entered
        // by, which is the next one down.
        if (keywords.hasNext()) {
            keywords.next();
        }
        boolean viaReference = false;
        while (schemas.hasNext()) {
            Schema schema = schemas.next();
            if (!schema.getSchemaContext().isNullableKeywordEnabled()) {
                // Crossed into a resource whose dialect has no nullable
                // keyword, where the member is only an annotation.
                return false;
            }
            if (!(viaReference && ignoresReferenceSiblings(schema))
                    && isNodeNullable(schema.getSchemaNode())) {
                return true;
            }
            if (!keywords.hasNext()) {
                return false;
            }
            String enteredBy = String.valueOf(keywords.next());
            if (REFERENCING_KEYWORDS.contains(enteredBy)) {
                viaReference = true;
            } else if (VALUE_PRESERVING_KEYWORDS.contains(enteredBy)) {
                viaReference = false;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Determines whether the schema's dialect drops members declared alongside
     * a reference, as the drafts before 2019-09 do. Mirrors the sibling
     * handling when a schema's validators are assembled.
     *
     * @param schema the schema holding the reference
     * @return true if members declared alongside a reference are dropped
     */
    private static boolean ignoresReferenceSiblings(Schema schema) {
        return schema.getSchemaContext().getDialect().getSpecificationVersion()
                .getOrder() < DRAFT_2019_09_VALUE;
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
            if (isNonFiniteNumber(node)) {
                return false;
            }
            return true;
        } else if (config.isTypeLoose()) {
            if (TypeFactory.getValueNodeType(node, config) == JsonType.STRING) {
                return Strings.isNumeric(node.asString());
            }
        }
        return false;
    }

    /**
     * Check if the node is a number and is one of NaN, Infinity or -Infinity
     * 
     * @param node to check
     * @return true if it is NaN, Infinity or -Infinity
     */
    public static boolean isNonFiniteNumber(JsonNode node) {
        if (node.isFloatingPointNumber() && !node.isBigDecimal() && !node.isBigInteger()
                && !Double.isFinite(node.doubleValue())) {
            return true;
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
