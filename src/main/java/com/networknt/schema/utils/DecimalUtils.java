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
}
