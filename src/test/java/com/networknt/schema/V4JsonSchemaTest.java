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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class V4JsonSchemaTest {

    protected ObjectMapper mapper = new ObjectMapper();

    @Test(/* expected = java.lang.StackOverflowError.class */)
    void testLoadingWithId() throws Exception {
        try (InputStream inputStream = new FileInputStream("src/test/resources/remotes/self_ref/selfRef.json")) {
            JsonNode schemaJson = mapper.readTree(inputStream);
            SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4);
            @SuppressWarnings("unused")
            Schema schema = factory.getSchema(schemaJson);
        }
    }

    /**
     * Although, the data file has three errors, but only on is reported
     */
    @Test
    void testFailFast_AllErrors() throws IOException {
        List<Error> messages = validateFailingFastSchemaFor("extra/product/product.schema.json",
                "extra/product/product-all-errors-data.json");
        assertEquals(1, messages.size());
    }

    /**
     * File contains only one error and that is reported.
     */
    @Test
    void testFailFast_OneErrors() throws IOException {
        List<Error> messages = validateFailingFastSchemaFor("extra/product/product.schema.json",
                "extra/product/product-one-error-data.json");
        assertEquals(1, messages.size());
    }

    /**
     * Although, the file contains two errors, but only one is reported
     */
    @Test
    void testFailFast_TwoErrors() throws IOException {
        List<Error> messages = validateFailingFastSchemaFor("extra/product/product.schema.json",
                "extra/product/product-two-errors-data.json");
        assertEquals(1, messages.size());
    }

    /**
     * The file contains no errors, in ths case
     * {@link Set}&lt;{@link Error}&gt; must be empty
     */
    @Test
    void testFailFast_NoErrors() throws IOException {
        final List<Error> messages = validateFailingFastSchemaFor("extra/product/product.schema.json",
                "extra/product/product-no-errors-data.json");
        assertTrue(messages.isEmpty());
    }

    private List<Error> validateFailingFastSchemaFor(final String schemaFileName, final String dataFileName) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode schema = getJsonNodeFromResource(objectMapper, schemaFileName);
        final JsonNode dataFile = getJsonNodeFromResource(objectMapper, dataFileName);
        final SchemaRegistryConfig config = SchemaRegistryConfig.builder().failFast(true).build();
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4, builder -> builder.schemaRegistryConfig(config))
            .getSchema(schema)
            .validate(dataFile);
    }

    private JsonNode getJsonNodeFromResource(final ObjectMapper mapper, final String locationInTestResources) throws IOException {
        return mapper.readTree(
            Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("draft4" + System.getProperty("file.separator") + locationInTestResources));

    }
}
