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

/** Exact finite decimal key for loose enum strings, without exponent expansion. */
final class NumericStringValue {
    private static final NumericStringValue ZERO = new NumericStringValue(0, "0", 0);
    // Saturation is far beyond any BigDecimal coefficient length/scale combination.
    private static final long EXPONENT_LIMIT = Long.MAX_VALUE / 2;
    private final int sign;
    private final String digits;
    private final long power;

    private NumericStringValue(int sign, String digits, long power) {
        this.sign = sign;
        this.digits = digits;
        this.power = power;
    }

    static NumericStringValue of(BigDecimal value) {
        if (value.signum() == 0) {
            return ZERO;
        }
        String digits = value.unscaledValue().abs().toString();
        int end = digits.length();
        while (digits.charAt(end - 1) == '0') {
            end--;
        }
        return new NumericStringValue(value.signum(), digits.substring(0, end),
                -(long) value.scale() + digits.length() - end);
    }

    /** Returns null unless the complete input follows the JSON number grammar. */
    static NumericStringValue parse(String text) {
        int length = text.length();
        if (length == 0) {
            return null;
        }
        int index = text.charAt(0) == '-' ? 1 : 0;
        int start = index;
        if (index == length) {
            return null;
        }
        char first = text.charAt(index++);
        if (first >= '1' && first <= '9') {
            while (index < length && isDigit(text.charAt(index))) {
                index++;
            }
        } else if (first != '0') {
            return null;
        }
        int point = -1;
        int fractionDigits = 0;
        if (index < length && text.charAt(index) == '.') {
            point = index++;
            int fractionStart = index;
            while (index < length && isDigit(text.charAt(index))) {
                index++;
            }
            fractionDigits = index - fractionStart;
            if (fractionDigits == 0) {
                return null;
            }
        }
        int significandEnd = index;
        long exponent = 0;
        if (index < length && (text.charAt(index) == 'e' || text.charAt(index) == 'E')) {
            index++;
            boolean negativeExponent = false;
            if (index < length && (text.charAt(index) == '+' || text.charAt(index) == '-')) {
                negativeExponent = text.charAt(index++) == '-';
            }
            int exponentStart = index;
            while (index < length && isDigit(text.charAt(index))) {
                int digit = text.charAt(index++) - '0';
                exponent = exponent > (EXPONENT_LIMIT - digit) / 10
                        ? EXPONENT_LIMIT : exponent * 10 + digit;
            }
            if (index == exponentStart) {
                return null;
            }
            if (negativeExponent) {
                exponent = -exponent;
            }
        }
        if (index != length) {
            return null;
        }
        int firstNonzero = -1;
        int lastNonzero = -1;
        for (int i = start; i < significandEnd; i++) {
            char digit = text.charAt(i);
            if (digit != '0' && digit != '.') {
                if (firstNonzero < 0) {
                    firstNonzero = i;
                }
                lastNonzero = i;
            }
        }
        if (firstNonzero < 0) {
            return ZERO;
        }
        String digits;
        if (point > firstNonzero && point < lastNonzero) {
            digits = text.substring(firstNonzero, point) + text.substring(point + 1, lastNonzero + 1);
        } else {
            digits = text.substring(firstNonzero, lastNonzero + 1);
        }
        int trailingZeros = significandEnd - lastNonzero - 1 - (point > lastNonzero ? 1 : 0);
        return new NumericStringValue(start == 0 ? 1 : -1, digits,
                exponent - fractionDigits + trailingZeros);
    }

    private static boolean isDigit(char value) {
        return value >= '0' && value <= '9';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NumericStringValue)) {
            return false;
        }
        NumericStringValue value = (NumericStringValue) other;
        return sign == value.sign && power == value.power && digits.equals(value.digits);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * sign + Long.hashCode(power)) + digits.hashCode();
    }
}
