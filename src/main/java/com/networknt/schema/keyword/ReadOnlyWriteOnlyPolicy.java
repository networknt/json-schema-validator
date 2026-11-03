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

import tools.jackson.databind.JsonNode;

/**
 * The readOnly and writeOnly execution policies, which turn those annotation
 * keywords into assertions.
 */
final class ReadOnlyWriteOnlyPolicy {
    private ReadOnlyWriteOnlyPolicy() {
    }

    /**
     * Whether the execution policy is in force.
     *
     * @param policy the config value of the policy, true turns the keyword into an
     *               assertion
     * @return whether the policy is in force
     */
    static boolean enabled(Boolean policy) {
        return Boolean.TRUE.equals(policy);
    }

    /**
     * Whether the policy asserts for the given value of the keyword.
     * <p>
     * The policy asserts only when it is in force and the keyword does not hold the
     * boolean false. The value of false is the only value that does not contradict
     * the annotation that is reported for the keyword, so it is the only value that
     * does not assert. Every other value asserts, including a value that is not a
     * boolean, which is not a valid keyword value.
     * <p>
     * The rule lives here rather than in either validator so that readOnly and
     * writeOnly cannot drift apart, and the policy value is part of the predicate so
     * that a caller cannot ask whether the keyword asserts while the policy is off.
     *
     * @param policy     the config value of the policy
     * @param schemaNode the schema node of the keyword, may be null
     * @return whether the policy asserts
     */
    static boolean asserts(Boolean policy, JsonNode schemaNode) {
        return enabled(policy) && valueAsserts(schemaNode);
    }

    /**
     * Whether the keyword value asserts, independent of the policy.
     *
     * @param schemaNode the schema node of the keyword, may be null
     * @return whether every value except the boolean false asserts
     */
    private static boolean valueAsserts(JsonNode schemaNode) {
        return schemaNode != null && !(schemaNode.isBoolean() && !schemaNode.booleanValue());
    }
}
