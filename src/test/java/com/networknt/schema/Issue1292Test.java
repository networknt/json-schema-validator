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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * minContains and maxContains each report only their own malformed value, under
 * their own keyword, and contradictory bounds are left to contains.
 */
class Issue1292Test {
    private static List<Error> validate(String schemaData, String inputData) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(schemaData);
        return schema.validate(inputData, InputFormat.JSON);
    }

    private static List<String> keywordsAndPaths(List<Error> errors) {
        return errors.stream().map(error -> error.getKeyword() + " " + error.getEvaluationPath())
                .collect(Collectors.toList());
    }

    @Test
    void malformedMinContainsIsReportedOnceUnderMinContains() {
        List<Error> errors = validate("{\"minContains\":-1,\"maxContains\":2}", "[]");
        assertEquals(Arrays.asList("minContains /minContains"), keywordsAndPaths(errors));
    }

    @Test
    void malformedMaxContainsIsReportedOnceUnderMaxContains() {
        List<Error> errors = validate("{\"minContains\":1,\"maxContains\":-1}", "[]");
        assertEquals(Arrays.asList("maxContains /maxContains"), keywordsAndPaths(errors));
    }

    @Test
    void bothMalformedBoundsAreEachReportedOnce() {
        List<Error> errors = validate("{\"minContains\":-1,\"maxContains\":-2}", "[]");
        assertEquals(Arrays.asList("minContains /minContains", "maxContains /maxContains"), keywordsAndPaths(errors));
    }

    @Test
    void minContainsAloneIsReportedUnderMinContains() {
        List<Error> errors = validate("{\"minContains\":-1}", "[]");
        assertEquals(Arrays.asList("minContains /minContains"), keywordsAndPaths(errors));
    }

    @Test
    void contradictoryBoundsOnAnArrayOnlyFailContains() {
        List<Error> errors = validate("{\"contains\":true,\"minContains\":2,\"maxContains\":1}", "[1]");
        assertEquals(Arrays.asList("minContains /contains"), keywordsAndPaths(errors));
        assertEquals(": must contain at least 2 element(s) that passes these validations: true",
                errors.get(0).toString());
    }

    @Test
    void contradictoryBoundsWithoutContainsPass() {
        assertEquals(Collections.emptyList(), validate("{\"minContains\":2,\"maxContains\":1}", "[1]"));
    }
}
