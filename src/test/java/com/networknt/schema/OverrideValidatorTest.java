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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.format.PatternFormat;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverrideValidatorTest {

    @Test
    void overrideDefaultValidator() throws JsonProcessingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final String URI = "https://github.com/networknt/json-schema-validator/tests/schemas/example01";
        final String schema = "{\n" +
        "  \"$schema\":\n" +
        "    \"https://github.com/networknt/json-schema-validator/tests/schemas/example01\",\n" +
        "  \"properties\": {\n" +
        "     \"mailaddress\": {\"type\": \"string\", \"format\": \"email\"},\n" +
        "     \"timestamp\": {\"type\": \"string\", \"format\": \"date-time\"}\n" +
        "  }\n" +
        "}";
        final JsonNode targetNode = objectMapper.readTree("{\n" +
        "  \"mailaddress\": \"a-zA-Z0-9.!#$%&'*+@a---a.a--a\",\n" +
        "  \"timestamp\": \"bad\"\n" +
        "}");
        // Use Default EmailValidator
        final JsonMetaSchema validatorMetaSchema = JsonMetaSchema
                .builder(URI, JsonMetaSchema.getV201909())
                .build();

        final JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(Specification.Version.DRAFT_2019_09)).metaSchema(validatorMetaSchema).build();
        final JsonSchema validatorSchema = validatorFactory.getSchema(schema);

        List<Error> messages = validatorSchema.validate(targetNode, OutputFormat.DEFAULT, (executionContext, validationContext) -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });

        assertEquals(2, messages.size(), Arrays.toString(messages.toArray()));
        assertTrue(messages.stream().anyMatch(it -> it.getInstanceLocation().getName(-1).equals("mailaddress")));
        assertTrue(messages.stream().anyMatch(it -> it.getInstanceLocation().getName(-1).equals("timestamp")));

        // Override EmailValidator
        final JsonMetaSchema overrideValidatorMetaSchema = JsonMetaSchema
                .builder(URI, JsonMetaSchema.getV201909())
                .format(PatternFormat.of("email", "^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$", "format.email"))
                .build();

        final JsonSchemaFactory overrideValidatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(Specification.Version.DRAFT_2019_09)).metaSchema(overrideValidatorMetaSchema).build();
        final JsonSchema overrideValidatorSchema = overrideValidatorFactory.getSchema(schema);

        messages = overrideValidatorSchema.validate(targetNode, executionContext -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertTrue(messages.stream().anyMatch(it -> it.getInstanceLocation().getName(-1).equals("timestamp")));
        assertEquals(1, messages.size());
    }
}