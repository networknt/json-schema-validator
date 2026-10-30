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

package com.networknt.schema.keyword;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.utils.JsonNodeTypes;
import com.networknt.schema.utils.JsonType;
import com.networknt.schema.utils.TypeFactory;
import com.networknt.schema.SchemaContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link KeywordValidator} for enum.
 */
public class EnumValidator extends BaseKeywordValidator implements KeywordValidator {
    private final Set<JsonNodeValue> nodes;
    private final String error;
    private final Set<String> looseValues;
    private final Set<NumericStringValue> looseNumbers;

    static String asString(JsonNode node) {
        if (node.isObject() || node.isArray() || node.isString() || node.isNull()) {
            // toString for isString is so that there are quotes
            return node.toString();
        }
        return node.asString();
    }
    
    public EnumValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.ENUM, schemaNode, schemaLocation, parentSchema, schemaContext);
        boolean typeLoose = schemaContext.getSchemaRegistryConfig().isTypeLoose();
        looseValues = typeLoose ? new HashSet<>() : Collections.emptySet();
        looseNumbers = typeLoose ? new HashSet<>() : Collections.emptySet();
        if (schemaNode != null && schemaNode.isArray()) {
            nodes = new HashSet<>();
            StringBuilder sb = new StringBuilder();

            sb.append('[');
            String separator = "";
            boolean hasNull = false;

            for (JsonNode n : schemaNode) {
                hasNull |= n.isNull();
                JsonNode converted = n;
                if (n.isNumber()) {
                    converted = processNumberNode(n);
                } else if (n.isArray()) {
                    converted = processArrayNode((ArrayNode) n);
                }
                boolean added = nodes.add(JsonNodeValue.of(converted));
                if (added && typeLoose) {
                    prepareLooseValue(converted);
                }

                sb.append(separator);
                sb.append(asString(n));
                separator = ", ";
            }

            // Whether null is accepted depends on the ancestors the value is
            // reached through, which is only known during validation. This
            // just reports null as permitted when the owning schema says so.
            if (!hasNull && schemaContext.isNullableKeywordEnabled()
                    && JsonNodeTypes.isNodeNullable(parentSchema.getSchemaNode())) {
                separator = ", ";
                sb.append(separator);
                sb.append("null");
            }
            sb.append(']');

            error = sb.toString();
        } else {
            nodes = Collections.emptySet();
            error = "[none]";
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        if (node.isNull() && this.schemaContext.isNullableKeywordEnabled()
                && JsonNodeTypes.isNullableAncestor(executionContext)) {
            return;
        }
        if (node.isNumber()) {
            node = processNumberNode(node);
        } else if (node.isArray()) {
            node = processArrayNode((ArrayNode) node);
        }
        if (!nodes.contains(JsonNodeValue.of(node)) && !( this.schemaContext.getSchemaRegistryConfig().isTypeLoose() && isTypeLooseContainsInEnum(node))) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale())
                    .arguments(error).build());
        }
    }

    /**
     * Check whether enum contains the value of the JsonNode if the typeLoose is enabled.
     *
     * @param node JsonNode to check
     */
    private boolean isTypeLooseContainsInEnum(JsonNode node) {
        if (TypeFactory.getValueNodeType(node, this.schemaContext.getSchemaRegistryConfig()) == JsonType.STRING) {
            String nodeText = node.asString();
            if (looseValues.contains(nodeText)) {
                return true;
            }
            return !looseNumbers.isEmpty() && looseNumbers.contains(NumericStringValue.parse(nodeText));
        }
        return false;
    }

    /** Prepare scalar loose comparison once; containers do not coerce to strings. */
    private void prepareLooseValue(JsonNode node) {
        if (node.isArray() || node.isObject()) {
            return;
        }
        if (node.isNumber() && !JsonNodeTypes.isNonFiniteNumber(node)) {
            looseNumbers.add(NumericStringValue.of(node.decimalValue()));
        } else {
            looseValues.add(node.asString());
        }
    }

    /**
     * Conversion hook for subclasses. The default retains the original node;
     * numeric value equality no longer needs normalized copies.
     *
     * @param n the node
     * @return the node
     */
    protected JsonNode processNumberNode(JsonNode n) {
        return n;
    }

    /**
     * Applies the numeric conversion hook, copying only when a member changes.
     * 
     * @param node the node
     * @return the node
     */
    protected ArrayNode processArrayNode(ArrayNode node) {
        if (!hasNumber(node)) {
            return node;
        }
        ArrayNode a = node;
        for (int x = 0; x < a.size(); x++) {
            JsonNode v = a.get(x);
            if (v.isNumber()) {
                v = processNumberNode(v);
                if (v != a.get(x)) {
                    if (a == node) {
                        a = node.deepCopy();
                    }
                    a.set(x, v);
                }
            }
        }
        return a;
    }

    /**
     * Determines if the array node contains a number.
     * 
     * @param node the node
     * @return the node
     */
    protected boolean hasNumber(ArrayNode node) {
        for (int x = 0; x < node.size(); x++) {
            JsonNode v = node.get(x);
            if (v.isNumber()) {
                return true;
            }
        }
        return false;
    }
}
