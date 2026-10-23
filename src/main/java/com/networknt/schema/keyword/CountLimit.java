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

/**
 * A count limit compared with int-sized instances without narrowing the schema
 * value. Large exponents stay in decimal form rather than expanding to integers.
 */
final class CountLimit {
    private final Number value;
    private final int intValue;
    private final int outsideIntRange;

    private CountLimit(Number value, int intValue, int outsideIntRange) {
        this.value = value;
        this.intValue = intValue;
        this.outsideIntRange = outsideIntRange;
    }

    static CountLimit of(int value) {
        return new CountLimit(Integer.valueOf(value), value, 0);
    }

    /** Infinite floating-point nodes represent limits outside any int-sized instance. */
    static boolean isIntegral(JsonNode node) {
        if (node.isDouble() || node.isFloat()) {
            // Avoid FloatNode's narrowing integral check. This includes infinities
            // for saturation and rejects NaN and fractional values by equality.
            double number = node.doubleValue();
            return number == Math.rint(number);
        }
        return node.isNumber() && node.canConvertToExactIntegral();
    }

    static CountLimit of(JsonNode node) {
        BigDecimal value;
        if (node.isDouble() || node.isFloat()) {
            double number = node.doubleValue();
            if (Double.isInfinite(number)) {
                return new CountLimit(node.numberValue(), number > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE,
                        number > 0 ? 1 : -1);
            }
            // Use the floating-point value's short decimal spelling, not its binary expansion.
            value = node.isFloat() ? new BigDecimal(Float.toString(node.floatValue())) : BigDecimal.valueOf(number);
            value = value.stripTrailingZeros();
        } else {
            value = node.decimalValue();
        }
        if (value.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            return new CountLimit(value, Integer.MAX_VALUE, 1);
        }
        if (value.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) < 0) {
            return new CountLimit(value, Integer.MIN_VALUE, -1);
        }
        return of(value.intValue());
    }

    /** Whether the actual count meets this minimum. */
    boolean isMinimumSatisfiedBy(int actual) {
        return this.outsideIntRange < 0 || (this.outsideIntRange == 0 && actual >= this.intValue);
    }

    /** Whether the actual count meets this maximum. */
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
        // Strings prevent locale grouping and expansion of exponent-sized numbers.
        if (this.value instanceof BigDecimal && ((BigDecimal) this.value).scale() != 0) {
            try {
                return ((BigDecimal) this.value).stripTrailingZeros().toString();
            } catch (ArithmeticException e) {
                // Stripping zeros can underflow BigDecimal's scale. Its original
                // scientific representation is still exact and bounded in size.
                return this.value.toString();
            }
        }
        return this.value.toString();
    }

    /** Saturated view for the legacy protected int field. */
    int intValue() {
        return this.intValue;
    }
}
