/*
 * Copyright (c) 2026 Network New Technologies Inc.
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

import java.util.function.Supplier;

import tools.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Error;

/**
 * The readOnly and writeOnly execution policies, which turn those annotation
 * keywords into assertions.
 */
final class ReadOnlyWriteOnlyPolicy {
    private ReadOnlyWriteOnlyPolicy() {
    }

    /**
     * Asserts unless the value of the keyword is the boolean false.
     * <p>
     * The value of false is the only value that does not contradict the annotation
     * that is reported for the keyword, so it is the only value that does not
     * assert. Every other value asserts, including a value that is not a boolean,
     * which is not a valid keyword value.
     * <p>
     * The rule lives here rather than in either validator so that readOnly and
     * writeOnly cannot drift apart. The caller supplies the error as a supplier,
     * because the two keywords have their own message and the error must not be
     * built when the rule does not assert.
     *
     * @param executionContext the execution context
     * @param policy           the config value of the policy, true turns the keyword
     *                         into an assertion
     * @param schemaNode       the schema node of the keyword
     * @param error            supplies the error to report when the rule asserts
     */
    static void assertUnlessFalse(ExecutionContext executionContext, Boolean policy, JsonNode schemaNode,
            Supplier<Error> error) {
        if (!Boolean.TRUE.equals(policy)) {
            return;
        }
        if (schemaNode.isBoolean() && !schemaNode.booleanValue()) {
            return;
        }
        executionContext.addError(error.get());
    }
}
