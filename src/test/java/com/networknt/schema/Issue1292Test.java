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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

import com.networknt.schema.keyword.KeywordType;
import com.networknt.schema.keyword.MinMaxContainsValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;
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
        assertEquals(List.of("minContains /minContains"), keywordsAndPaths(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void malformedMaxContainsIsReportedOnceUnderMaxContains(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"minContains\":1,\"maxContains\":-1}", "[]");
        assertEquals(List.of("maxContains /maxContains"), keywordsAndPaths(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void bothMalformedBoundsAreEachReportedOnce(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"minContains\":-1,\"maxContains\":-2}", "[]");
        assertEquals(List.of("minContains /minContains", "maxContains /maxContains"), keywordsAndPaths(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void minContainsAloneIsReportedUnderMinContains(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"minContains\":-1}", "[]");
        assertEquals(List.of("minContains /minContains"), keywordsAndPaths(errors));
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void contradictoryBoundsOnAnArrayOnlyFailContains(SpecificationVersion version) {
        List<Error> errors = validate(version, "{\"contains\":true,\"minContains\":2,\"maxContains\":1}", "[1]");
        assertEquals(List.of("minContains /contains"), keywordsAndPaths(errors));
        assertEquals(": must contain at least 2 element(s) that passes these validations: true",
                errors.get(0).toString());
    }

    @ParameterizedTest
    @EnumSource(value = SpecificationVersion.class, names = { "DRAFT_2019_09", "DRAFT_2020_12" })
    void contradictoryBoundsWithoutContainsPass(SpecificationVersion version) {
        assertEquals(List.of(), validate(version, "{\"minContains\":2,\"maxContains\":1}", "[1]"));
    }

    /**
     * The deprecated 4-argument constructor works out the keyword from the location.
     */
    @ParameterizedTest
    @CsvSource({
            "minContains, '{\"minContains\":-1,\"maxContains\":2}', 1",
            "minContains, '{\"minContains\":1,\"maxContains\":-1}', 0",
            "maxContains, '{\"minContains\":1,\"maxContains\":-1}', 1",
            "maxContains, '{\"minContains\":-1,\"maxContains\":2}', 0" })
    @SuppressWarnings("deprecation")
    void deprecatedConstructorChecksTheKeywordAtItsLocation(String keyword, String schemaData, int expectedErrors) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(schemaData);
        MinMaxContainsValidator validator = new MinMaxContainsValidator(schema.getSchemaLocation().append(keyword),
                schema.getSchemaNode().get(keyword), schema, schema.getSchemaContext());
        assertEquals(keyword, validator.getKeyword());

        ExecutionContext executionContext = schema.createExecutionContext();
        JsonNode instance = JsonNodeFactory.instance.arrayNode();
        validator.validate(executionContext, instance, instance, new NodePath(PathType.JSON_POINTER));
        List<String> keywords = executionContext.getErrors().stream().map(Error::getKeyword)
                .collect(Collectors.toList());
        assertEquals(Collections.nCopies(expectedErrors, keyword), keywords);
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
        assertEquals(List.of("maxContains", "minContains"), walkedKeywords);
    }
}
