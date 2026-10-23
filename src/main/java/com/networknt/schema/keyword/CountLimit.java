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
import com.fasterxml.jackson.databind.JsonNode;

/** Immutable count comparison, annotation value and preformatted error argument. */
final class CountLimit {
    private static final BigDecimal INT_MIN = BigDecimal.valueOf(Integer.MIN_VALUE);
    private static final BigDecimal INT_MAX = BigDecimal.valueOf(Integer.MAX_VALUE);
    private final Number value;
    private final int intValue;
    private final int outsideIntRange;
    private final String messageArgument;

    private CountLimit(Number value, int intValue, int outsideIntRange, String messageArgument) {
        this.value = value;
        this.intValue = intValue;
        this.outsideIntRange = outsideIntRange;
        this.messageArgument = messageArgument;
    }

    static CountLimit of(int value) {
        return new CountLimit(Integer.valueOf(value), value, 0, Integer.toString(value));
    }

    static CountLimit of(JsonNode node, int fallback) {
        CountLimit parsed = parse(node);
        return parsed == null ? of(fallback) : parsed;
    }

    /** Parses once, returning null for an absent, nonnumeric or fractional bound. */
    static CountLimit parse(JsonNode node) {
        if (node == null || !node.isNumber()) {
            return null;
        }
        BigDecimal decimal;
        Number annotation = node.numberValue();
        if (node.isDouble() || node.isFloat()) {
            double number = node.doubleValue();
            if (Double.isInfinite(number)) {
                return new CountLimit(annotation, number > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE,
                        number > 0 ? 1 : -1, number > 0 ? "Infinity" : "-Infinity");
            }
            if (number != Math.rint(number)) {
                return null; // Includes NaN.
            }
            decimal = node.isFloat() ? new BigDecimal(Float.toString(node.floatValue())) : BigDecimal.valueOf(number);
        } else {
            decimal = node.decimalValue();
        }
        BigDecimal normalized;
        try {
            normalized = decimal.stripTrailingZeros();
        } catch (ArithmeticException e) {
            // Already integral: stripping zeros at the minimum scale underflows.
            normalized = decimal;
        }
        if (normalized.scale() > 0) {
            return null;
        }
        int range = decimal.compareTo(INT_MAX) > 0 ? 1 : decimal.compareTo(INT_MIN) < 0 ? -1 : 0;
        int integer = range > 0 ? Integer.MAX_VALUE : range < 0 ? Integer.MIN_VALUE : decimal.intValue();
        return new CountLimit(annotation, integer, range,
                range == 0 ? Integer.toString(integer)
                        : format(node, normalized));
    }

    private static String format(JsonNode node, BigDecimal value) {
        // Bound zero expansion for decimal/float inputs independently of node scale.
        // Integer nodes already contain their full digits and preserve that contract.
        long digits = (long) value.precision() - value.scale();
        if (node.isIntegralNumber() || digits <= 100) {
            return value.toPlainString();
        }
        if (value.scale() < 0) {
            return value.toString();
        }
        // Scientific notation without expanding an extreme exponent or losing digits.
        return new BigDecimal(value.unscaledValue(), value.precision() - 1).toPlainString()
                + "E+" + (digits - 1);
    }

    boolean isMinimumSatisfiedBy(int actual) {
        return this.outsideIntRange < 0 || (this.outsideIntRange == 0 && actual >= this.intValue);
    }

    boolean isMaximumSatisfiedBy(int actual) {
        return this.outsideIntRange > 0 || (this.outsideIntRange == 0 && actual <= this.intValue);
    }

    boolean isNegative() {
        return this.outsideIntRange < 0 || (this.outsideIntRange == 0 && this.intValue < 0);
    }

    Number value() {
        return this.value;
    }

    String messageArgument() {
        return this.messageArgument;
    }

    int intValue() {
        return this.intValue;
    }
}
