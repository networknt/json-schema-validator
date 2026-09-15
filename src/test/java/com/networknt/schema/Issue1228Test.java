/*
 * Copyright (c) 2026 the original author or authors.
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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class Issue1228Test {
    private final Schema schema = SchemaRegistry
            .withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
            .getSchema("""
                    {
                      "type": "object",
                      "properties": {
                        "defaultValue": { "type": "number" }
                      }
                    }
                    """);

    @ParameterizedTest
    @CsvSource({
            ".nan, NaN",
            ".inf, Infinity",
            "-.inf, -Infinity"
    })
    void yamlNonFiniteValuesReturnTypeErrors(String literal, double expectedValue) {
        List<Error> errors = schema.validate("defaultValue: " + literal, InputFormat.YAML);

        assertEquals(1, errors.size());
        Error error = errors.get(0);
        assertEquals("type", error.getKeyword());
        assertEquals("/defaultValue", error.getInstanceLocation().toString());
        assertTrue(error.getInstanceNode().isNumber(),
                "The YAML literal should be parsed as a numeric node");
        assertEquals(expectedValue, error.getInstanceNode().doubleValue());
    }

    @Test
    void finiteYamlNumberIsValid() {
        assertTrue(schema.validate("defaultValue: 1.5", InputFormat.YAML).isEmpty());
    }

    @Test
    void quotedNanRemainsAString() {
        List<Error> errors = schema.validate("defaultValue: '.nan'", InputFormat.YAML);

        assertEquals(1, errors.size());
        assertEquals("type", errors.get(0).getKeyword());
        assertEquals("/defaultValue", errors.get(0).getInstanceLocation().toString());
        assertTrue(errors.get(0).getInstanceNode().isString());
        assertEquals(".nan", errors.get(0).getInstanceNode().stringValue());
    }
}
