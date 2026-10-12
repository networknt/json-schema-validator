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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.networknt.schema.keyword.KeywordType;
import com.networknt.schema.walk.KeywordWalkHandler;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import com.networknt.schema.walk.WalkListener;

/**
 * minContains and maxContains each report only their own malformed value, under
 * their own keyword, and contradictory bounds are left to contains.
 */
class Issue1292Test {
    private static List<Error> validate(SpecificationVersion version, String schemaData, String inputData) {
        Schema schema = SchemaRegistry.withDefaultDialect(version).getSchema(schemaData);
        return schema.validate(inputData, InputFormat.JSON);
    }

    private static List<String> keywordsAndPaths(List<Error> errors) {
        return errors.stream().map(error -> error.getKeyword() + " " + error.getEvaluationPath())
                .collect(Collectors.toList());
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void malformedMinContainsIsReportedOnceUnderMinContains(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"minContains\":-1,\"maxContains\":2}", "[]");
        assertEquals(Arrays.asList("minContains /minContains"), keywordsAndPaths(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void malformedMaxContainsIsReportedOnceUnderMaxContains(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"minContains\":1,\"maxContains\":-1}", "[]");
        assertEquals(Arrays.asList("maxContains /maxContains"), keywordsAndPaths(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void bothMalformedBoundsAreEachReportedOnce(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"minContains\":-1,\"maxContains\":-2}", "[]");
        assertEquals(Arrays.asList("minContains /minContains", "maxContains /maxContains"), keywordsAndPaths(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void minContainsAloneIsReportedUnderMinContains(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"minContains\":-1}", "[]");
        assertEquals(Arrays.asList("minContains /minContains"), keywordsAndPaths(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void contradictoryBoundsOnAnArrayOnlyFailContains(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"contains\":true,\"minContains\":2,\"maxContains\":1}", "[1]");
        assertEquals(Arrays.asList("minContains /contains"), keywordsAndPaths(errors));
        assertEquals(": must contain at least 2 element(s) that passes these validations: true",
                errors.get(0).toString());
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void contradictoryBoundsWithoutContainsPass(SpecificationVersion version) {
        assertEquals(Arrays.asList(), validate(version, "{\"minContains\":2,\"maxContains\":1}", "[1]"));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void walkListenersFireOnceForEachKeyword(SpecificationVersion version) {
        List<String> walkedKeywords = new ArrayList<>();
        WalkListener listener = new WalkListener() {
            @Override
            public WalkFlow onWalkStart(WalkEvent walkEvent) {
                walkedKeywords.add(walkEvent.getKeyword());
                return WalkFlow.CONTINUE;
            }

            @Override
            public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
            }
        };
        KeywordWalkHandler keywordWalkHandler = KeywordWalkHandler.builder()
                .keywordWalkListener(KeywordType.MIN_CONTAINS.getValue(), listener)
                .keywordWalkListener(KeywordType.MAX_CONTAINS.getValue(), listener)
                .build();
        Schema schema = SchemaRegistry.withDefaultDialect(version).getSchema("{\"minContains\":1,\"maxContains\":2}");
        schema.walk("[1]", InputFormat.JSON, true, executionContext -> executionContext
                .walkConfig(walkConfig -> walkConfig.keywordWalkHandler(keywordWalkHandler)));
        walkedKeywords.sort(null);
        assertEquals(Arrays.asList("maxContains", "minContains"), walkedKeywords);
    }
}
