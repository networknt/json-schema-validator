/*
 * Copyright (c) 2024 the original author or authors.
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

import org.junit.jupiter.api.Test;

import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.serialization.NodeReader;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Test ExclusiveMaximumValidator validator.
 */
class ExclusiveMaximumValidatorTest {
    @Test
    void exclusiveMaximum() {
        final String schemaString = """
        {
            "$schema": "http://json-schema.org/draft-07/schema#",
            "type": ["null", "integer"],
            "exclusiveMaximum": 10
        }
        """;
        final SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft7());
        assertEquals(1, schemaRegistry.getSchema(schemaString).validate("10", InputFormat.JSON).size());
    }

    @Test
    void nonFinite() {
        String schemaData = "{\r\n"
                + "  \"exclusiveMaximum\": 10\r\n"
                + "}";
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4,
                builder -> builder.nodeReader(NodeReader.builder()
                        .jsonMapper(JsonMapper.builder().enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS).build())
                        .build()))
                .getSchema(schemaData);
        List<Error> errors = schema.validate("NaN", InputFormat.JSON);
        assertEquals(0, errors.size());
        errors = schema.validate("Infinity", InputFormat.JSON);
        assertEquals(0, errors.size());
        errors = schema.validate("-Infinity", InputFormat.JSON);
        assertEquals(0, errors.size());
    }
}
