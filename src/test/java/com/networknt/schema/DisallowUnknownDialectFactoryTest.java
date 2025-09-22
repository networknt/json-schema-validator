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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.networknt.schema.Specification.Version;
import com.networknt.schema.dialect.DisallowUnknownDialectFactory;

/**
 * Tests for DisallowUnknownJsonMetaSchemaFactory. 
 */
class DisallowUnknownDialectFactoryTest {
    private static final String DRAFT_202012_SCHEMA = "{\r\n"
            + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
            + "  \"type\": \"object\"\r\n"
            + "}"; 

    private static final String DRAFT_7_SCHEMA = "{\r\n"
            + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
            + "  \"type\": \"object\"\r\n"
            + "}";
    @Test
    void defaultHandling() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(Version.DRAFT_2020_12);
        assertDoesNotThrow(() -> factory.getSchema(DRAFT_202012_SCHEMA));
        assertDoesNotThrow(() -> factory.getSchema(DRAFT_7_SCHEMA));
    }

    @Test
    void draft202012() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(Version.DRAFT_2020_12,
                builder -> builder.metaSchemaFactory(DisallowUnknownDialectFactory.getInstance()));
        assertDoesNotThrow(() -> factory.getSchema(DRAFT_202012_SCHEMA));
        assertThrows(InvalidSchemaException.class, () -> factory.getSchema(DRAFT_7_SCHEMA));
    }

    @Test
    void draft7() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(Version.DRAFT_7,
                builder -> builder.metaSchemaFactory(DisallowUnknownDialectFactory.getInstance()));
        assertDoesNotThrow(() -> factory.getSchema(DRAFT_7_SCHEMA));
        assertThrows(InvalidSchemaException.class, () -> factory.getSchema(DRAFT_202012_SCHEMA));
    }
}
