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
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.utils.JsonNodeTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link KeywordValidator} for uniqueItems.
 */
public class UniqueItemsValidator extends BaseKeywordValidator implements KeywordValidator {
    private final boolean unique;

    public UniqueItemsValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.UNIQUE_ITEMS, schemaNode, schemaLocation, parentSchema, schemaContext);
        if (schemaNode.isBoolean()) {
            unique = schemaNode.booleanValue();
        } else {
            unique = false;
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        if (unique) {
            Set<Object> set = new HashSet<>();
            for (JsonNode n : node) {
                if (!set.add(comparisonKey(n))) {
                    executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                            .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale())
                            .build());
                }
            }
        }
    }

    /**
     * Builds the value this keyword compares items by.
     *
     * uniqueItems treats two numbers as the same item when they are
     * mathematically equal, so 1, 1.0 and 1.00 are one value. JsonNode equality
     * is type-sensitive -- IntNode(1) does not equal DoubleNode(1.0) -- so
     * comparing the nodes themselves let that duplicate through. Numbers are
     * therefore reduced to a scale-independent BigDecimal.
     *
     * NaN, Infinity and -Infinity have no BigDecimal form, so they are left out
 * of that and keep comparing as nodes, as every item did before.
 *
 * Only numbers are folded together. A number and a boolean stay distinct,
     * as do a number and its string form, which the suite requires: nested [1]
     * and [true], and [0] and [false], are unique arrays. Objects and arrays are
     * walked so the rule holds wherever the number sits.
     */
    private static Object comparisonKey(JsonNode node) {
        if (node.isNumber() && !JsonNodeTypes.isNonFiniteNumber(node)) {
            return node.decimalValue().stripTrailingZeros();
        }
        if (node.isArray()) {
            List<Object> items = new ArrayList<>(node.size());
            for (JsonNode item : node) {
                items.add(comparisonKey(item));
            }
            return items;
        }
        if (node.isObject()) {
            // Map equality ignores insertion order, which is what JSON objects want.
            Map<String, Object> properties = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> property : node.properties()) {
                properties.put(property.getKey(), comparisonKey(property.getValue()));
            }
            return properties;
        }
        // Strings, booleans and null compare as themselves; a node is its own key.
        return node;
    }

}
