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

package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.dialect.Dialects;

/**
 * Tests the errors {@code oneOf} and {@code anyOf} report with an OpenAPI
 * discriminator, with and without fail fast.
 */
class AlternativeDiscriminatorTest {

    // The BedRoom branch matches the second room, but its discriminator value
    // maps to no schema, so the oneOf or anyOf adds a discriminator error. The
    // maxItems keyword would add another error if evaluation went on after it.
    private static final String SCHEMA = """
            {
              "type": "array",
              "allOf": [
                {
                  "items": {
                    "%s": [
                      { "$ref": "#/components/schemas/Kitchen" },
                      { "$ref": "#/components/schemas/BedRoom" }
                    ]
                  }
                },
                { "maxItems": 1 }
              ],
              "components": {
                "schemas": {
                  "Room": {
                    "type": "object",
                    "properties": { "@type": { "type": "string" } },
                    "required": ["@type"],
                    "discriminator": { "propertyName": "@type" }
                  },
                  "BedRoom": {
                    "type": "object",
                    "allOf": [
                      { "$ref": "#/components/schemas/Room" },
                      { "type": "object", "required": ["numberOfBeds"] }
                    ]
                  },
                  "Kitchen": {
                    "type": "object",
                    "allOf": [
                      { "$ref": "#/components/schemas/Room" },
                      { "type": "object", "required": ["hasMicrowaveOven"] }
                    ]
                  }
                }
              }
            }
            """;

    private static final String UNMAPPED_DISCRIMINATOR_INPUT = """
            [
              { "@type": "Kitchen", "hasMicrowaveOven": true },
              { "@type": "BedRooooom", "numberOfBeds": 4 }
            ]
            """;

    // The Kitchen branch is the one the discriminator selects, but it misses
    // hasMicrowaveOven, so no branch matches.
    private static final String DISCRIMINATED_BRANCH_FAILING_INPUT = """
            [ { "@type": "Kitchen" } ]
            """;

    private static Schema schema(String keyword, boolean failFast) {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().failFast(failFast).build();
        return SchemaRegistry.withDialect(Dialects.getOpenApi31(), builder -> builder.schemaRegistryConfig(config))
                .getSchema(SCHEMA.formatted(keyword));
    }

    private static List<String> keywords(List<Error> errors) {
        return errors.stream().map(Error::getKeyword).toList();
    }

    @ParameterizedTest
    @ValueSource(strings = { "oneOf", "anyOf" })
    void failFastStopsAtDiscriminatorError(String keyword) {
        List<Error> messages = schema(keyword, true).validate(UNMAPPED_DISCRIMINATOR_INPUT, InputFormat.JSON);
        assertEquals(List.of("discriminator"), keywords(messages));
    }

    @ParameterizedTest
    @ValueSource(strings = { "oneOf", "anyOf" })
    void reportsDiscriminatorErrorThroughExecutionContext(String keyword) {
        List<Error> reported = new ArrayList<>();
        ExecutionContext executionContext = new ExecutionContext() {
            @Override
            public void addError(Error error) {
                reported.add(error);
                super.addError(error);
            }
        };
        List<Error> messages = schema(keyword, false).validate(executionContext, UNMAPPED_DISCRIMINATOR_INPUT,
                InputFormat.JSON);
        assertTrue(keywords(messages).contains("discriminator"));
        assertTrue(reported.containsAll(messages));
    }

    // oneOf reports its own error before the errors of the discriminated
    // branch; anyOf has no error of its own and reports only the branch errors.
    @ParameterizedTest
    @CsvSource({ "oneOf, oneOf required", "anyOf, required" })
    void reportsDiscriminatedBranchErrors(String keyword, String expectedKeywords) {
        List<Error> messages = schema(keyword, false).validate(DISCRIMINATED_BRANCH_FAILING_INPUT, InputFormat.JSON);
        assertEquals(List.of(expectedKeywords.split(" ")), keywords(messages));
    }

    // With fail fast each returns its first error: oneOf its own error, anyOf
    // the first error of the discriminated branch.
    @ParameterizedTest
    @CsvSource({ "oneOf, oneOf", "anyOf, required" })
    void failFastWithDiscriminatorReturnsFirstError(String keyword, String expectedKeyword) {
        List<Error> messages = schema(keyword, true).validate(DISCRIMINATED_BRANCH_FAILING_INPUT, InputFormat.JSON);
        assertEquals(List.of(expectedKeyword), keywords(messages));
    }
}
