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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.networknt.schema.path.PathType;

class Issue1270Test {

    private static final String INVALID_PROPERTY_SCHEMA = "{\"type\":\"object\",\"properties\":{\"required\":[]}}";

    @Test
    void schemaExceptionShouldHonorJsonPathLocationFormatting() {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().pathType(PathType.JSON_PATH).build();
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7,
                builder -> builder.schemaRegistryConfig(config));

        SchemaException exception = assertThrows(SchemaException.class,
                () -> registry.getSchema(INVALID_PROPERTY_SCHEMA));

        assertTrue(exception.getMessage().contains("must be object or boolean"), exception.getMessage());
        assertTrue(exception.getMessage().contains("ARRAY"), exception.getMessage());
        assertTrue(exception.getMessage().contains("$.properties.required"), exception.getMessage());
        assertFalse(exception.getMessage().contains("#/properties/required"), exception.getMessage());
    }

    @Test
    void schemaExceptionShouldKeepJsonPointerByDefault() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);

        SchemaException exception = assertThrows(SchemaException.class,
                () -> registry.getSchema(INVALID_PROPERTY_SCHEMA));

        assertTrue(exception.getMessage().contains("must be object or boolean"), exception.getMessage());
        assertTrue(exception.getMessage().contains("#/properties/required"), exception.getMessage());
    }
}
