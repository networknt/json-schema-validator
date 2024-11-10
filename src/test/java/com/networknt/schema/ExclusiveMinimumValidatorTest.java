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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * Test ExclusiveMinimumValidator validator.
 */
class ExclusiveMinimumValidatorTest {
    @Test
    void draftV4ShouldHaveExclusiveMinimum() {
        String schemaData = "{" +
                "  \"type\": \"object\"," +
                "  \"properties\": {" +
                "    \"value1\": {" +
                "      \"type\": \"number\"," +
                "      \"minimum\": 0," +
                "      \"exclusiveMinimum\": true" +
                "    }" +
                "  }" +
                "}";        
        JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV4())
                .unknownKeywordFactory(DisallowUnknownKeywordFactory.getInstance()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V4,
                builder -> builder.metaSchema(metaSchema));
        JsonSchema schema = factory.getSchema(schemaData);
        String inputData = "{\"value1\":0}";
        String validData = "{\"value1\":0.1}";
        
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals(1, messages.stream().filter(m -> "minimum".equals(m.getType())).count());
        
        messages = schema.validate(validData, InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void draftV6ShouldNotAllowExclusiveMinimumBoolean() {
        String schemaData = "{" +
                "  \"type\": \"object\"," +
                "  \"properties\": {" +
                "    \"value1\": {" +
                "      \"type\": \"number\"," +
                "      \"minimum\": 0," +
                "      \"exclusiveMinimum\": true" +
                "    }" +
                "  }" +
                "}";
        JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV6())
                .unknownKeywordFactory(DisallowUnknownKeywordFactory.getInstance()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V6,
                builder -> builder.metaSchema(metaSchema));
        assertThrows(JsonSchemaException.class, () -> factory.getSchema(schemaData));
    }

    @Test
    void draftV7ShouldNotAllowExclusiveMinimumBoolean() {
        String schemaData = "{" +
                "  \"type\": \"object\"," +
                "  \"properties\": {" +
                "    \"value1\": {" +
                "      \"type\": \"number\"," +
                "      \"minimum\": 0," +
                "      \"exclusiveMinimum\": true" +
                "    }" +
                "  }" +
                "}";
        JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV7())
                .unknownKeywordFactory(DisallowUnknownKeywordFactory.getInstance()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7,
                builder -> builder.metaSchema(metaSchema));
        assertThrows(JsonSchemaException.class, () -> factory.getSchema(schemaData));
    }
}
