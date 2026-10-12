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

import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.KeywordType;

import org.junit.jupiter.api.Test;
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
        assertEquals(Collections.emptyList(), validate(version, "{\"contains\":true,\"maxContains\":0}", "[1]"));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_6", "DRAFT_7" })
    void minContainsIsIgnored(SpecificationVersion version) {
        assertEquals(Collections.emptyList(), validate(version, "{\"contains\":{\"const\":1},\"minContains\":2}", "[1]"));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_6", "DRAFT_7" })
    void moreMatchesThanMaxContainsAreAccepted(SpecificationVersion version) {
        assertEquals(Collections.emptyList(), validate(version, "{\"contains\":{\"const\":1},\"maxContains\":0}", "[1,1]"));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_6", "DRAFT_7" })
    void bothBoundsAreIgnored(SpecificationVersion version) {
        assertEquals(Collections.emptyList(),
                validate(version, "{\"contains\":{\"const\":1},\"minContains\":3,\"maxContains\":0}", "[1,1]"));
    }

    /**
     * A custom dialect based on Draft 7 that adds minContains and maxContains
     * enforces them.
     */
    @Test
    void customDraft7DialectWithBoundsEnforcesThem() {
        Dialect dialect = Dialect.builder(Dialects.getDraft7()).keyword(KeywordType.MIN_CONTAINS)
                .keyword(KeywordType.MAX_CONTAINS).build();
        SchemaRegistry registry = SchemaRegistry.withDialect(dialect);

        List<Error> minErrors = registry.getSchema("{\"contains\":{\"const\":1},\"minContains\":2}")
                .validate("[1]", InputFormat.JSON);
        assertEquals(Arrays.asList(
                "minContains /contains : must contain at least 2 element(s) that passes these validations: {\"const\":1}"),
                describe(minErrors));

        List<Error> maxErrors = registry.getSchema("{\"contains\":true,\"maxContains\":1}")
                .validate("[1,2]", InputFormat.JSON);
        assertEquals(Arrays.asList("maxContains /contains : must contain at most 1 element(s) that passes these validations: true"),
                describe(maxErrors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void minContainsIsStillEnforced(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"contains\":{\"const\":1},\"minContains\":2}", "[1]");
        assertEquals(Arrays.asList(
                "minContains /contains : must contain at least 2 element(s) that passes these validations: {\"const\":1}"),
                describe(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void maxContainsIsStillEnforced(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"contains\":true,\"maxContains\":1}", "[1,2]");
        assertEquals(Arrays.asList("maxContains /contains : must contain at most 1 element(s) that passes these validations: true"),
                describe(errors));
    }
}
