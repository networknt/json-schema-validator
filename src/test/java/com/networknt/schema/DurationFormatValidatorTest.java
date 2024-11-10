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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DurationFormatValidatorTest {

    @Test
    void durationFormatValidatorTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final String schema = "{\"type\": \"string\", \"format\": \"duration\"}\n";
        final JsonNode validTargetNode = objectMapper.readTree("\"P1D\"");
        final JsonNode invalidTargetNode = objectMapper.readTree("\"INVALID_DURATION\"");

        final JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).build();
        final JsonSchema validatorSchema = validatorFactory.getSchema(schema);

        Set<ValidationMessage> messages = validatorSchema.validate(validTargetNode);
        assertEquals(0, messages.size());

        messages = validatorSchema.validate(invalidTargetNode, OutputFormat.DEFAULT, (executionContext, validationContext) -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertEquals(1, messages.size());

    }
}
