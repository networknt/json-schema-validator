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

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * minContains and maxContains only apply from Draft 2019-09. In Draft 6 and 7,
 * contains only needs one matching element.
 */
class Issue1300Test {
    private static List<Error> validate(SpecificationVersion version, String schemaData, String inputData) {
        Schema schema = SchemaRegistry.withDefaultDialect(version).getSchema(schemaData);
        return schema.validate(inputData, InputFormat.JSON);
    }

    private static List<String> describe(List<Error> errors) {
        return errors.stream().map(error -> error.getKeyword() + " " + error.getEvaluationPath() + " " + error)
                .collect(Collectors.toList());
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_6", "DRAFT_7" })
    void maxContainsIsIgnored(SpecificationVersion version) {
        assertEquals(List.of(), validate(version, "{\"contains\":true,\"maxContains\":0}", "[1]"));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_6", "DRAFT_7" })
    void minContainsIsIgnored(SpecificationVersion version) {
        assertEquals(List.of(), validate(version, "{\"contains\":{\"const\":1},\"minContains\":2}", "[1]"));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_6", "DRAFT_7" })
    void malformedBoundsAreIgnored(SpecificationVersion version) {
        assertEquals(List.of(),
                validate(version, "{\"contains\":{\"const\":1},\"minContains\":-1,\"maxContains\":\"x\"}", "[1]"));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_6", "DRAFT_7" })
    void noMatchIsReportedOnceUnderContains(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"contains\":{\"const\":1},\"minContains\":2,\"maxContains\":0}",
                "[2]");
        assertEquals(List.of(
                "contains /contains : does not contain an element that passes these validations: {\"const\":1}"),
                describe(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void minContainsIsStillEnforced(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"contains\":{\"const\":1},\"minContains\":2}", "[1]");
        assertEquals(List.of(
                "minContains /contains : must contain at least 2 element(s) that passes these validations: {\"const\":1}"),
                describe(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void maxContainsIsStillEnforced(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"contains\":true,\"maxContains\":1}", "[1,2]");
        assertEquals(List.of("maxContains /contains : must contain at most 1 element(s) that passes these validations: true"),
                describe(errors));
    }
}
