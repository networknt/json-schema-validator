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

import com.fasterxml.jackson.databind.JsonNode;

/** An integral count bound and its unsaturated error argument. */
final class CountLimit {
    final int value;
    final Object argument;
    private final boolean exceedsIntegerRange;

    CountLimit(JsonNode node, int fallback) {
        if (node == null || !node.canConvertToExactIntegral()) {
            this.value = fallback;
            this.argument = fallback;
            this.exceedsIntegerRange = false;
        } else if (node.canConvertToInt()) {
            this.value = node.intValue();
            this.argument = this.value;
            this.exceedsIntegerRange = false;
        } else {
            this.exceedsIntegerRange = node.decimalValue().signum() > 0;
            this.value = this.exceedsIntegerRange ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            this.argument = node.asText();
        }
    }

    boolean isBelowMinimum(int count) {
        // A count of Integer.MAX_VALUE still fails a larger minimum.
        return this.exceedsIntegerRange || count < this.value;
    }
}
