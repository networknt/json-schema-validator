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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeType;

/**
 * Value equality and hashing for const, enum and uniqueItems. Numbers compare by
 * their decimal value, including inside arrays and objects. In particular, a
 * floating-point node uses its shortest decimal form, not its exact binary value.
 * Only numeric types are folded together; strings and booleans remain distinct.
 * Arrays and objects are walked without making normalized copies.
 *
 * A key caches the kind and value of a top-level number, so repeated hash-bucket
 * comparisons do not classify it again. Large integer pairs compare directly
 * without allocating BigDecimals, and nested container keys stay small.
 * Referenced nodes must not be mutated while a key is in a set.
 */
class JsonNodeValue {
    final JsonNode node;
    private final int hash;

    static JsonNodeValue of(JsonNode node) {
        if (!node.isNumber()) {
            return new JsonNodeValue(node, hash(node));
        }
        int kind = numberKind(node);
        long value = kind == WHOLE ? wholeValue(node)
                : kind == DOUBLE || kind == NON_FINITE ? Double.doubleToLongBits(node.doubleValue()) : 0;
        return new NumberValue(node, kind, value);
    }

    private JsonNodeValue(JsonNode node, int hash) {
        this.node = node;
        this.hash = hash;
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JsonNodeValue)) {
            return false;
        }
        JsonNodeValue other = (JsonNodeValue) obj;
        return hash == other.hash && same(node, other.node);
    }

    /** Only top-level numbers pay for cached classification and primitive data. */
    private static final class NumberValue extends JsonNodeValue {
        private final int kind;
        // A WHOLE value, or the canonical bits of a DOUBLE/NON_FINITE value.
        private final long value;

        NumberValue(JsonNode node, int kind, long value) {
            super(node, kind == BIG ? bigHash(decimalValue(node)) : Long.hashCode(value));
            this.kind = kind;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof NumberValue)) {
                return false;
            }
            NumberValue other = (NumberValue) obj;
            if (hashCode() != other.hashCode()) {
                return false;
            }
            if (kind == WHOLE || other.kind == WHOLE || kind == NON_FINITE || other.kind == NON_FINITE) {
                return kind == other.kind && value == other.value;
            }
            if (kind == DOUBLE && other.kind == DOUBLE) {
                return value == other.value;
            }
            if (node.isBigInteger() && other.node.isBigInteger()) {
                return node.bigIntegerValue().equals(other.node.bigIntegerValue());
            }
            return decimalValue(node).compareTo(decimalValue(other.node)) == 0;
        }
    }

    /** Decimal value policy shared with enum conversion hooks. */
    static BigDecimal decimalValue(JsonNode node) {
        return node.decimalValue();
    }

    /**
     * Canonicalize without expanding the exponent or underflowing the scale.
     * If stripping all zeros would underflow, retain the zeros that cannot be
     * represented at Integer.MIN_VALUE. Equal values still get the same hash.
     */
    static BigDecimal normalize(BigDecimal value) {
        try {
            return value.stripTrailingZeros();
        } catch (ArithmeticException e) {
            int removable = (int) ((long) value.scale() - Integer.MIN_VALUE);
            return new BigDecimal(value.unscaledValue().divide(BigInteger.TEN.pow(removable)), Integer.MIN_VALUE);
        }
    }

    private static int bigHash(BigDecimal value) {
        // Jackson 3 can throw for node.doubleValue() outside the double range.
        double d = value.doubleValue();
        if (Double.isFinite(d) && BigDecimal.valueOf(d).compareTo(value) == 0) {
            return Double.hashCode(d);
        }
        return normalize(value).hashCode();
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
            // at or above 2^53 in magnitude is whole, and strictly between -2^63
            // and 2^63 its shortest form is a whole number that fits in a long.
            // -2^63 itself is left out: its shortest form can be
            // -9223372036854776000, which is below Long.MIN_VALUE.
            return value == Math.rint(value) && value > -0x1p63 && value < 0x1p63 ? WHOLE : DOUBLE;
        case BIG_INTEGER:
            return node.canConvertToLong() ? WHOLE : BIG;
        default:
            BigDecimal decimal = decimalValue(node);
            boolean whole = decimal.signum() == 0 || decimal.scale() <= 0 || decimal.stripTrailingZeros().scale() <= 0;
            return whole && decimal.compareTo(LONG_MIN) >= 0 && decimal.compareTo(LONG_MAX) <= 0 ? WHOLE : BIG;
        }
    }

    /** The value of a {@link #WHOLE} number. */
    private static long wholeValue(JsonNode node) {
        if (node.isDouble() || node.isFloat()) {
            double value = node.doubleValue();
            // 0x1p53 is 2^53. Below it a whole double's shortest form is its exact
            // value; above it the two can differ (2^60 is not its shortest form).
            return Math.abs(value) < 0x1p53 ? (long) value : decimalValue(node).longValue();
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
                return Double.hashCode(node.doubleValue());
            case DOUBLE:
                return Double.hashCode(node.doubleValue());
            default: // BIG
                return bigHash(decimalValue(node));
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

    static boolean same(JsonNode a, JsonNode b) {
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
            return aKind == bKind && Double.compare(a.doubleValue(), b.doubleValue()) == 0;
        }
        if (aKind == DOUBLE && bKind == DOUBLE) {
            return a.doubleValue() == b.doubleValue();
        }
        return decimalValue(a).compareTo(decimalValue(b)) == 0;
    }

}
