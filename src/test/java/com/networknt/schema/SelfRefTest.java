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

package com.networknt.schema;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import tools.jackson.databind.JsonNode;

/**
 * Created by stevehu on 2016-12-20.
 *
 * Regression coverage for a recursive {@code $ref} (a self-referential "tree"
 * schema). This historically caused a {@code StackOverflowError} while loading
 * the schema; the test guards against that and additionally verifies that
 * validation actually recurses into nested branches.
 */
class SelfRefTest extends BaseJsonSchemaValidatorTest {

    @Test
    void recursiveRefLoadsWithoutStackOverflow() {
        Schema schema = getJsonSchemaFromClasspath("selfRef.json");
        Assertions.assertNotNull(schema);
    }

    @Test
    void recursiveRefValidatesNestedBranches() throws Exception {
        Schema schema = getJsonSchemaFromClasspath("selfRef.json");

        // A well-formed, deeply nested tree: every node carries the required
        // "value", so recursing through "branches" should yield no errors.
        JsonNode valid = getJsonNodeFromStringContent(
                "{\"name\":\"root\",\"tree\":{\"value\":\"a\",\"branches\":["
                + "{\"value\":\"b\",\"branches\":[{\"value\":\"c\"}]}]}}");
        List<Error> validErrors = schema.validate(valid);
        Assertions.assertTrue(validErrors.isEmpty(),
                "valid recursive tree should not produce errors, got: " + validErrors);

        // A nested branch missing the required "value" must be reported, which
        // only happens if the validator follows the recursive $ref downward.
        JsonNode invalid = getJsonNodeFromStringContent(
                "{\"name\":\"root\",\"tree\":{\"value\":\"a\",\"branches\":["
                + "{\"branches\":[{\"value\":\"c\"}]}]}}");
        List<Error> invalidErrors = schema.validate(invalid);
        Assertions.assertFalse(invalidErrors.isEmpty(),
                "missing required 'value' in a nested branch should be reported");
    }
}
