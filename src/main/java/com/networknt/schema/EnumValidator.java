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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link JsonValidator} for enum.
 */
public class EnumValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(EnumValidator.class);

    private final Set<JsonNode> nodes;
    private final String error;

    static String asText(JsonNode node) {
        if (node.isObject() || node.isArray() || node.isTextual()) {
            // toString for isTextual is so that there are quotes
            return node.toString();
        }
        return node.asText();
    }
    
    public EnumValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ENUM, validationContext);
        if (schemaNode != null && schemaNode.isArray()) {
            nodes = new HashSet<>();
            StringBuilder sb = new StringBuilder();

            sb.append('[');
            String separator = "";

            for (JsonNode n : schemaNode) {
                if (n.isNumber()) {
                    // convert to DecimalNode for number comparison
                    nodes.add(processNumberNode(n));
                } else if (n.isArray()) {
                    ArrayNode a = processArrayNode((ArrayNode) n);
                    nodes.add(a);
                } else {
                    nodes.add(n);
                }

                sb.append(separator);
                sb.append(asText(n));
                separator = ", ";
            }

            // check if the parent schema declares the fields as nullable
            if (validationContext.getConfig().isNullableKeywordEnabled()) {
                JsonNode nullable = parentSchema.getSchemaNode().get("nullable");
                if (nullable != null && nullable.asBoolean()) {
                    nodes.add(NullNode.getInstance());
                    separator = ", ";
                    sb.append(separator);
                    sb.append("null");
                }
            }
            sb.append(']');

            error = sb.toString();
        } else {
            nodes = Collections.emptySet();
            error = "[none]";
        }
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        if (node.isNumber()) {
            node = processNumberNode(node);
        } else if (node.isArray()) {
            node = processArrayNode((ArrayNode) node);
        }
        if (!nodes.contains(node) && !( this.validationContext.getConfig().isTypeLoose() && isTypeLooseContainsInEnum(node))) {
            return Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .failFast(executionContext.isFailFast()).arguments(error).build());
        }

        return Collections.emptySet();
    }

    /**
     * Check whether enum contains the value of the JsonNode if the typeLoose is enabled.
     *
     * @param node JsonNode to check
     */
    private boolean isTypeLooseContainsInEnum(JsonNode node) {
        if (TypeFactory.getValueNodeType(node, this.validationContext.getConfig()) == JsonType.STRING) {
            String nodeText = node.textValue();
            for (JsonNode n : nodes) {
                String value = n.asText();
                if (value != null && value.equals(nodeText)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Processes the number and ensures trailing zeros are stripped.
     * 
     * @param n the node
     * @return the node
     */
    protected JsonNode processNumberNode(JsonNode n) {
        return DecimalNode.valueOf(new BigDecimal(n.decimalValue().toPlainString()));
    }

    /**
     * Processes the array and ensures that numbers within have trailing zeroes stripped.
     * 
     * @param node the node
     * @return the node
     */
    protected ArrayNode processArrayNode(ArrayNode node) {
        if (!hasNumber(node)) {
            return node;
        }
        ArrayNode a = node.deepCopy();
        for (int x = 0; x < a.size(); x++) {
            JsonNode v = a.get(x);
            if (v.isNumber()) {
                v = processNumberNode(v);
                a.set(x, v);
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
