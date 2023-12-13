/*
 * Copyright (c) 2023 the original author or authors.
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

import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.i18n.Locales;

public class LocaleTest {
    private JsonSchema getSchema(SchemaValidatorsConfig config) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        return factory.getSchema(
                "{ \"$schema\": \"https://json-schema.org/draft/2019-09/schema\", \"$id\": \"https://json-schema.org/draft/2019-09/schema\", \"type\": \"object\", \"properties\": { \"foo\": { \"type\": \"string\" } } } }",
                config);
    }

    /**
     * Tests that the validation messages are generated based on the execution
     * context locale.
     * 
     * @throws JsonMappingException    the error
     * @throws JsonProcessingException the error
     */
    @Test
    void executionContextLocale() throws JsonMappingException, JsonProcessingException {
        JsonNode rootNode = new ObjectMapper().readTree(" { \"foo\": 123 } ");
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        JsonSchema jsonSchema = getSchema(config);

        Locale locale = Locales.findSupported("it;q=0.9,fr;q=1.0"); // fr
        ExecutionContext executionContext = jsonSchema.createExecutionContext();
        assertEquals(config.getLocale(), executionContext.getExecutionConfig().getLocale());
        executionContext.getExecutionConfig().setLocale(locale);
        Set<ValidationMessage> messages = jsonSchema.validate(executionContext, rootNode);
        assertEquals(1, messages.size());
        assertEquals("$.foo: integer a été trouvé, mais string est attendu", messages.iterator().next().getMessage());

        locale = Locales.findSupported("it;q=1.0,fr;q=0.9"); // it
        executionContext = jsonSchema.createExecutionContext();
        assertEquals(config.getLocale(), executionContext.getExecutionConfig().getLocale());
        executionContext.getExecutionConfig().setLocale(locale);
        messages = jsonSchema.validate(executionContext, rootNode);
        assertEquals(1, messages.size());
        assertEquals("$.foo: integer trovato, string atteso", messages.iterator().next().getMessage());
    }
}
