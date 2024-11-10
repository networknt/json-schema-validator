/*
 * Copyright (c) 2020 Network New Technologies Inc.
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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory.Builder;
import com.networknt.schema.resource.MapSchemaMapper;
import com.networknt.schema.resource.SchemaMapper;

class UriMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Validate that a JSON URI Mapping file containing the URI Mapping schema is
     * schema valid.
     *
     * @throws IOException if unable to parse the mapping file
     */
    @Test
    void testBuilderUriMappingUri() throws IOException {
        URL mappings = UriMappingTest.class.getResource("/draft4/extra/uri_mapping/uri-mapping.json");
        JsonMetaSchema draftV4 = JsonMetaSchema.getV4();
        Builder builder = JsonSchemaFactory.builder()
                .defaultMetaSchemaIri(draftV4.getIri())
                .metaSchema(draftV4)
                .schemaMappers(schemaMappers -> schemaMappers.add(getUriMappingsFromUrl(mappings)));
        JsonSchemaFactory instance = builder.build();
        JsonSchema schema = instance.getSchema(SchemaLocation.of(
                "https://raw.githubusercontent.com/networknt/json-schema-validator/master/src/test/resources/draft4/extra/uri_mapping/uri-mapping.schema.json"));
        assertEquals(0, schema.validate(mapper.readTree(mappings)).size());
    }

    /**
     * Validate that local URI is used when attempting to get a schema that is not
     * available publicly. Use the URL http://example.com/invalid/schema/url to use
     * a URL that returns a 404 Not Found. The locally mapped schema is a
     * valid, but empty schema.
     *
     * @throws IOException if unable to parse the mapping file
     */
    @Test
    void testBuilderExampleMappings() throws IOException {
        JsonSchemaFactory instance = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        SchemaLocation example = SchemaLocation.of("https://example.com/invalid/schema/url");
        // first test that attempting to use example URL throws an error
        try {
            JsonSchema schema = instance.getSchema(example);
            schema.validate(mapper.createObjectNode());
            fail("Expected exception not thrown");
        } catch (JsonSchemaException ex) {
            Throwable cause = ex.getCause();
            if (!(cause instanceof IOException )) {
                fail("Unexpected cause for JsonSchemaException", ex);
            }
            // passing, so do nothing
        } catch (Exception ex) {
            fail("Unexpected exception thrown", ex);
        }
        URL mappings = UriMappingTest.class.getResource("/draft4/extra/uri_mapping/invalid-schema-uri.json");
        JsonMetaSchema draftV4 = JsonMetaSchema.getV4();
        Builder builder = JsonSchemaFactory.builder()
                .defaultMetaSchemaIri(draftV4.getIri())
                .metaSchema(draftV4)
                .schemaMappers(schemaMappers -> schemaMappers.add(getUriMappingsFromUrl(mappings)));
        instance = builder.build();
        JsonSchema schema = instance.getSchema(example);
        assertEquals(0, schema.validate(mapper.createObjectNode()).size());
    }

    /**
     * Validate that a JSON URI Mapping file containing the URI Mapping schema is
     * schema valid.
     *
     * @throws IOException if unable to parse the mapping file
     */
    @Test
    void testValidatorConfigUriMappingUri() throws IOException {
        URL mappings = UriMappingTest.class.getResource("/draft4/extra/uri_mapping/uri-mapping.json");
        JsonSchemaFactory instance = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4))
                .schemaMappers(schemaMappers -> schemaMappers.add(getUriMappingsFromUrl(mappings))).build();
        JsonSchema schema = instance.getSchema(SchemaLocation.of(
                "https://raw.githubusercontent.com/networknt/json-schema-validator/master/src/test/resources/draft4/extra/uri_mapping/uri-mapping.schema.json"));
        assertEquals(0, schema.validate(mapper.readTree(mappings)).size());
    }

    /**
     * Validate that local URL is used when attempting to get a schema that is not
     * available publicly. Use the URL http://example.com/invalid/schema/url to use
     * a URL that returns a 404 Not Found. The locally mapped schema is a
     * valid, but empty schema.
     *
     * @throws IOException if unable to parse the mapping file
     */
    @Test
    void testValidatorConfigExampleMappings() throws IOException {
        URL mappings = UriMappingTest.class.getResource("/draft4/extra/uri_mapping/invalid-schema-uri.json");
        JsonSchemaFactory instance = JsonSchemaFactory
                .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4)).build();
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        SchemaLocation example = SchemaLocation.of("https://example.com/invalid/schema/url");
        // first test that attempting to use example URL throws an error
        try {
            JsonSchema schema = instance.getSchema(example, config);
            schema.validate(mapper.createObjectNode());
            fail("Expected exception not thrown");
        } catch (JsonSchemaException ex) {
            Throwable cause = ex.getCause();
            if (!(cause instanceof IOException)) {
                fail("Unexpected cause for JsonSchemaException");
            }
            // passing, so do nothing
        } catch (Exception ex) {
            fail("Unexpected exception thrown");
        }
        instance = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4))
                .schemaMappers(schemaMappers -> schemaMappers.add(getUriMappingsFromUrl(mappings))).build();
        JsonSchema schema = instance.getSchema(example, config);
        assertEquals(0, schema.validate(mapper.createObjectNode()).size());
    }

    @Test
    void testMappingsForRef() throws IOException {
        URL mappings = UriMappingTest.class.getResource("/draft4/extra/uri_mapping/schema-with-ref-mapping.json");
        JsonSchemaFactory instance = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4))
                .schemaMappers(schemaMappers -> schemaMappers.add(getUriMappingsFromUrl(mappings))).build();
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = instance.getSchema(SchemaLocation.of("resource:draft4/extra/uri_mapping/schema-with-ref.json"),
                config);
        assertEquals(0, schema.validate(mapper.readTree("[]")).size());
    }

    private SchemaMapper getUriMappingsFromUrl(URL url) {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            for (JsonNode mapping : mapper.readTree(url)) {
                map.put(mapping.get("publicURL").asText(),
                        mapping.get("localURL").asText());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new MapSchemaMapper(map);
    }
}
