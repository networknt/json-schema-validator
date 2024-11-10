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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * Tests for meta schema validating a schema.
 */
class MetaSchemaValidationTest {
    /**
     * Validates a OpenAPI 3.1 schema using the OpenAPI 3.1 meta schema.
     *
     * @throws IOException the exception
     */
    @Test
    void oas31() throws IOException {
        try (InputStream input = MetaSchemaValidationTest.class.getResourceAsStream("/schema/oas/3.1/petstore.json")) {
            JsonNode inputData = JsonMapperFactory.getInstance().readTree(input);
            SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
            JsonSchema schema = JsonSchemaFactory
                    .getInstance(VersionFlag.V202012,
                            builder -> builder.schemaMappers(schemaMappers -> schemaMappers
                                    .mapPrefix("https://spec.openapis.org/oas/3.1", "classpath:oas/3.1")))
                    .getSchema(SchemaLocation.of("https://spec.openapis.org/oas/3.1/schema-base/2022-10-07"), config);
            Set<ValidationMessage> messages = schema.validate(inputData);
            assertEquals(0, messages.size());
        }
    }
}
