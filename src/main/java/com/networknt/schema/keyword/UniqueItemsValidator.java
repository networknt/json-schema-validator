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
import tools.jackson.databind.node.JsonNodeType;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;

import java.math.BigDecimal;
import java.util.HashSet;
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
        

        if (unique && node.isArray()) {
            Set<ItemKey> set = new HashSet<>();
            for (JsonNode n : node) {
                if (!set.add(new ItemKey(n))) {
                    executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                            .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale())
                            .build());
                }
            }
        }
    }

    /**
     * The value this keyword compares items by.
     *
     * uniqueItems treats two numbers as the same item when they are
     * mathematically equal, so 1, 1.0 and 1.00 are one value. JsonNode equality
     * is type-sensitive -- IntNode(1) does not equal DoubleNode(1.0) -- so the
     * nodes can't be compared directly. This key walks the node instead and
     * compares numbers by value, without copying arrays or objects.
     *
     * A number's value is its {@code decimalValue()}, which for a double is its
     * shortest decimal form, so a double read from 0.1 equals a BigDecimal 0.1.
     * Numbers whose value is whole and fits in a long are compared as longs, and
     * other doubles as doubles. Only a BigDecimal or BigInteger that isn't a long
     * is compared as a BigDecimal. NaN, Infinity and -Infinity have no BigDecimal
     * form, so they keep comparing as nodes.
     *
     * Only numbers are folded together. A number and a boolean stay distinct,
     * as do a number and its string form, which the suite requires: nested [1]
     * and [true], and [0] and [false], are unique arrays. Objects and arrays are
     * walked so the rule holds wherever the number sits.
     */
    private static final class ItemKey {
        private final JsonNode node;
        private final int hash;

        ItemKey(JsonNode node) {
            this.node = node;
            this.hash = hash(node);
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ItemKey)) {
                return false;
            }
            ItemKey other = (ItemKey) obj;
            return this.hash == other.hash && same(this.node, other.node);
        }
    }

    private static final BigDecimal LONG_MIN = BigDecimal.valueOf(Long.MIN_VALUE);
    private static final BigDecimal LONG_MAX = BigDecimal.valueOf(Long.MAX_VALUE);

    /** A whole number that fits in a long. */
    private static final int WHOLE = 0;
    /** Any other finite double or float. */
    private static final int DOUBLE = 1;
    /** Any other BigInteger or BigDecimal. */
    private static final int BIG = 2;
    /** NaN, Infinity or -Infinity. */
    private static final int NON_FINITE = 3;

    private static int numberKind(JsonNode node) {
        switch (node.numberType()) {
        case INT:
        case LONG:
            return WHOLE;
        case FLOAT:
        case DOUBLE:
            double value = node.doubleValue();
            if (!Double.isFinite(value)) {
                return NON_FINITE;
            }
            // 0x1p63 is 2^63, the first double above Long.MAX_VALUE. Every double
            // at or above 2^53 is whole, and its shortest form is a whole number
            // that still fits in a long below 2^63.
            return value == Math.rint(value) && value >= -0x1p63 && value < 0x1p63 ? WHOLE : DOUBLE;
        case BIG_INTEGER:
            return node.canConvertToLong() ? WHOLE : BIG;
        default:
            BigDecimal decimal = node.decimalValue();
            boolean whole = decimal.signum() == 0 || decimal.scale() <= 0 || decimal.stripTrailingZeros().scale() <= 0;
            return whole && decimal.compareTo(LONG_MIN) >= 0 && decimal.compareTo(LONG_MAX) <= 0 ? WHOLE : BIG;
        }
    }

    /** The value of a {@link #WHOLE} number. */
    private static long wholeValue(JsonNode node) {
        if (node.isDouble() || node.isFloat()) {
            double value = node.doubleValue();
            // 0x1p53 is 2^53. Below it a whole double's shortest form is its exact
            // value; above it the two can differ (2^60 is 1.15292150460684698E18).
            return Math.abs(value) < 0x1p53 ? (long) value : node.decimalValue().longValue();
        }
        return node.longValue();
    }

    private static int hash(JsonNode node) {
        switch (node.getNodeType()) {
        case NUMBER:
            switch (numberKind(node)) {
            case WHOLE:
                return Long.hashCode(wholeValue(node));
            case NON_FINITE:
                return node.hashCode();
            case DOUBLE:
                return Double.hashCode(node.doubleValue());
            default: // BIG
                // Not doubleValue() on the node: Jackson 3 throws when the value is
                // outside the double range. BigDecimal returns +/-Infinity instead.
                BigDecimal value = node.decimalValue();
                double d = value.doubleValue();
                if (Double.isFinite(d) && BigDecimal.valueOf(d).compareTo(value) == 0) {
                    // Equal to a double's value, so it must share that double's hash.
                    return Double.hashCode(d);
                }
                // No double has this value. Hashing its own exact value keeps
                // distinct numbers that round to the same double apart.
                return value.stripTrailingZeros().hashCode();
            }
        case ARRAY:
            int arrayHash = 1;
            for (int i = 0; i < node.size(); i++) {
                arrayHash = 31 * arrayHash + hash(node.get(i));
            }
            return arrayHash;
        case OBJECT:
            // A sum doesn't depend on property order, which is what JSON objects want.
            int objectHash = 0;
            for (Map.Entry<String, JsonNode> property : node.properties()) {
                objectHash += property.getKey().hashCode() ^ hash(property.getValue());
            }
            return objectHash;
        default:
            // Strings, booleans and null compare as themselves.
            return node.hashCode();
        }
    }

    private static boolean same(JsonNode a, JsonNode b) {
        JsonNodeType type = a.getNodeType();
        if (type != b.getNodeType()) {
            return false;
        }
        switch (type) {
        case NUMBER:
            return sameNumber(a, b);
        case ARRAY:
            if (a.size() != b.size()) {
                return false;
            }
            for (int i = 0; i < a.size(); i++) {
                if (!same(a.get(i), b.get(i))) {
                    return false;
                }
            }
            return true;
        case OBJECT:
            if (a.size() != b.size()) {
                return false;
            }
            for (Map.Entry<String, JsonNode> property : a.properties()) {
                JsonNode other = b.get(property.getKey());
                if (other == null || !same(property.getValue(), other)) {
                    return false;
                }
            }
            return true;
        default:
            return a.equals(b);
        }
    }

    private static boolean sameNumber(JsonNode a, JsonNode b) {
        int aKind = numberKind(a);
        int bKind = numberKind(b);
        if (aKind == WHOLE || bKind == WHOLE) {
            return aKind == bKind && wholeValue(a) == wholeValue(b);
        }
        if (aKind == NON_FINITE || bKind == NON_FINITE) {
            return a.equals(b);
        }
        if (aKind == DOUBLE && bKind == DOUBLE) {
            return a.doubleValue() == b.doubleValue();
        }
        return a.decimalValue().compareTo(b.decimalValue()) == 0;
    }

}
