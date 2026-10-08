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

package com.networknt.schema.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

import tools.jackson.databind.JsonNode;

/** Decimal operations that preserve values at the limits of BigDecimal's scale. */
public final class DecimalUtils {
    private DecimalUtils() {
    }

    /**
     * Returns a canonical representation without overflowing the scale or
     * repeatedly dividing very large coefficients once for every trailing zero.
     * Normalization stops at the minimum scale, preserving numeric equality and
     * the hash contract for all representable values.
     *
     * @param value the decimal to normalize
     * @return a numerically equal, canonical decimal
     */
    public static BigDecimal normalize(BigDecimal value) {
        if (value.signum() == 0) {
            return BigDecimal.ZERO;
        }
        long capacity = (long) value.scale() - Integer.MIN_VALUE;
        if (capacity == 0) {
            return value;
        }
        BigInteger coefficient = value.unscaledValue();
        if (coefficient.testBit(0)) {
            return value;
        }
        if (coefficient.bitLength() < 63 && capacity >= 18) {
            return value.stripTrailingZeros();
        }
        int stripped = 0;
        while (stripped < 18 && stripped < capacity) {
            if (coefficient.testBit(0)) {
                break;
            }
            BigInteger[] parts = coefficient.divideAndRemainder(BigInteger.TEN);
            if (parts[1].signum() != 0) {
                break;
            }
            coefficient = parts[0];
            stripped++;
        }
        if (stripped == 18 && stripped < capacity) {
            String digits = coefficient.toString();
            int end = digits.length();
            while (stripped < capacity && digits.charAt(end - 1) == '0') {
                end--;
                stripped++;
            }
            if (end != digits.length()) {
                coefficient = new BigInteger(digits.substring(0, end));
            }
        }
        return stripped == 0 ? value : new BigDecimal(coefficient, value.scale() - stripped);
    }

    /**
     * Converts a finite numeric node, preserving a float's own decimal spelling.
     * @param node the finite numeric node
     * @return its decimal value
     */
    public static BigDecimal decimalValue(JsonNode node) {
        // FloatNode.decimalValue() widens to double; preserve the float's decimal spelling.
        return node.isFloat() ? new BigDecimal(node.asString()) : node.decimalValue();
    }

    /**
     * Compares a finite numeric node or a numeric string with a decimal threshold.
     * String exponents may exceed BigDecimal's scale range.
     * @param node the number or numeric string
     * @param threshold the finite threshold
     * @return the comparison result
     */
    public static int compare(JsonNode node, BigDecimal threshold) {
        if (node.isNumber()) {
            return decimalValue(node).compareTo(threshold);
        }
        String text = node.asString();
        try {
            return new BigDecimal(text).compareTo(threshold);
        } catch (NumberFormatException exception) {
            return DecimalNumber.parse(text).compareTo(threshold);
        }
    }

    /** A decimal number whose exponent need not fit BigDecimal's scale. */
    public static final class DecimalNumber {
        private final BigDecimal significand;
        private final BigInteger exponent;

        private DecimalNumber(BigDecimal significand, BigInteger exponent) {
            this.significand = significand;
            this.exponent = exponent;
        }

        /**
         * Parses a decimal coefficient and an arbitrary-size base-ten exponent.
         * @param text the numeric text
         * @return the decomposed decimal number
         */
        public static DecimalNumber parse(String text) {
            int separator = text.indexOf('e');
            if (separator < 0) {
                separator = text.indexOf('E');
            }
            return separator < 0
                    ? new DecimalNumber(new BigDecimal(text), BigInteger.ZERO)
                    : new DecimalNumber(new BigDecimal(text.substring(0, separator)),
                            new BigInteger(text.substring(separator + 1)));
        }

        /** @return the decimal coefficient */
        public BigDecimal getSignificand() {
            return significand;
        }

        /** @return the base-ten exponent */
        public BigInteger getExponent() {
            return exponent;
        }

        private int compareTo(BigDecimal threshold) {
            int sign = this.significand.signum();
            int otherSign = threshold.signum();
            if (sign != otherSign) {
                return Integer.compare(sign, otherSign);
            }
            if (sign == 0) {
                return 0;
            }
            BigInteger magnitude = this.exponent.add(BigInteger.valueOf(
                    (long) this.significand.precision() - this.significand.scale()));
            int comparison = magnitude.compareTo(BigInteger.valueOf(
                    (long) threshold.precision() - threshold.scale()));
            if (comparison != 0) {
                return sign * comparison;
            }
            // Equal magnitudes need only the existing coefficients, never an exponent-sized power.
            return new BigDecimal(this.significand.unscaledValue(), this.significand.precision())
                    .compareTo(new BigDecimal(threshold.unscaledValue(), threshold.precision()));
        }
    }
}
