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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OverrideValidatorTest {

    @Test
    void overrideDefaultValidator() throws JsonProcessingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final String URI = "https://github.com/networknt/json-schema-validator/tests/schemas/example01";
        final String schema = "{\n" +
        "  \"$schema\":\n" +
        "    \"https://github.com/networknt/json-schema-validator/tests/schemas/example01\",\n" +
        "  \"properties\": {\"mailaddress\": {\"type\": \"string\", \"format\": \"email\"}}\n" +
        "}";
        final JsonNode targetNode = objectMapper.readTree("{\"mailaddress\": \"a-zA-Z0-9.!#$%&'*+@a---a.a--a\"}");
        // Use Default EmailValidator
        final JsonMetaSchema validatorMetaSchema = JsonMetaSchema
                .builder(URI, JsonMetaSchema.getV201909())
                .build();

        final JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).metaSchema(validatorMetaSchema).build();
        final JsonSchema validatorSchema = validatorFactory.getSchema(schema);

        Set<ValidationMessage> messages = validatorSchema.validate(targetNode, OutputFormat.DEFAULT, (executionContext, validationContext) -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertEquals(1, messages.size());

        // Override EmailValidator
        final JsonMetaSchema overrideValidatorMetaSchema = JsonMetaSchema
                .builder(URI, JsonMetaSchema.getV201909())
                .format(PatternFormat.of("email", "^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$", "format.email"))
                .build();

        final JsonSchemaFactory overrideValidatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).metaSchema(overrideValidatorMetaSchema).build();
        final JsonSchema overrideValidatorSchema = overrideValidatorFactory.getSchema(schema);

        messages = overrideValidatorSchema.validate(targetNode);
        assertEquals(0, messages.size());


    }
}
