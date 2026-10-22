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

import tools.jackson.databind.JsonNode;

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

    static CountLimit of(JsonNode node) {
        BigDecimal value = node.decimalValue();
        if (value.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            return new CountLimit(value, Integer.MAX_VALUE, 1);
        }
        if (value.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) < 0) {
            return new CountLimit(value, Integer.MIN_VALUE, -1);
        }
        return of(value.intValue());
    }

    /** Compares the schema limit to an actual count. */
    int compareTo(int actual) {
        return this.outsideIntRange != 0 ? this.outsideIntRange : Integer.compare(this.intValue, actual);
    }

    Number value() {
        return this.value;
    }

    Object messageArgument() {
        // MessageFormat expands Number arguments, even for exponent-sized limits.
        return this.outsideIntRange == 0 ? this.value : this.value.toString();
    }

    /** Saturated view for the legacy protected int field. */
    int intValue() {
        return this.intValue;
    }
}
