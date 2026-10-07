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

import tools.jackson.databind.JsonNode;

/** Numeric comparisons and loose numbers whose exponents exceed BigDecimal's scale. */
final class DecimalNumber {
    final BigDecimal significand;
    final BigInteger exponent;

    private DecimalNumber(BigDecimal significand, BigInteger exponent) {
        this.significand = significand;
        this.exponent = exponent;
    }

    static DecimalNumber parse(String text) {
        int separator = text.indexOf('e');
        if (separator < 0) {
            separator = text.indexOf('E');
        }
        return separator < 0
                ? new DecimalNumber(new BigDecimal(text), BigInteger.ZERO)
                : new DecimalNumber(new BigDecimal(text.substring(0, separator)),
                        new BigInteger(text.substring(separator + 1)));
    }

    static BigDecimal decimalValue(JsonNode node) {
        // FloatNode.decimalValue() widens to double; preserve the float's decimal spelling.
        return node.isFloat() ? new BigDecimal(node.asString()) : node.decimalValue();
    }

    static int compare(JsonNode node, BigDecimal threshold) {
        if (node.isNumber()) {
            return decimalValue(node).compareTo(threshold);
        }
        String text = node.asString();
        try {
            return new BigDecimal(text).compareTo(threshold);
        } catch (NumberFormatException exception) {
            return parse(text).compareTo(threshold);
        }
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
