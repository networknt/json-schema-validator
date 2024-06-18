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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.i18n.ResourceBundleMessageSource;

/**
 * Test for ConstValidator.
 */
class ConstValidatorTest {

    @Test
    void localeMessageOthers() {
        String schemaData = "{\r\n"
                + "  \"const\": \"aa\"\r\n"
                + "}";
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .messageSource(new ResourceBundleMessageSource("const-messages-override", "jsv-messages"))
                .build();
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData, config);
        String inputData = "\"bb\"";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(": must be the constant value 'aa' but is 'bb'", messages.iterator().next().getMessage());
    }

    @Test
    void localeMessageNumber() {
        String schemaData = "{\r\n"
                + "  \"const\": 1\r\n"
                + "}";
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .messageSource(new ResourceBundleMessageSource("const-messages-override", "jsv-messages"))
                .build();
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData, config);
        String inputData = "2";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(": must be the constant value '1' but is '2'", messages.iterator().next().getMessage());
    }

    @Test
    void validOthers() {
        String schemaData = "{\r\n"
                + "  \"const\": \"aa\"\r\n"
                + "}";
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData, config);
        String inputData = "\"aa\"";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertTrue(messages.isEmpty());
    }

    @Test
    void validNumber() {
        String schemaData = "{\r\n"
                + "  \"const\": 1234.56789\r\n"
                + "}";
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData, config);
        String inputData = "1234.56789";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertTrue(messages.isEmpty());
    }

    @Test
    void invalidNumber() {
        String schemaData = "{\r\n"
                + "  \"const\": 1234.56789\r\n"
                + "}";
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData, config);
        String inputData = "\"1234.56789\"";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
    }

}
