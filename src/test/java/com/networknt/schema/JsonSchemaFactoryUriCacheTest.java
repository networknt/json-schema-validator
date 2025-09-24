package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.dialect.BasicDialectRegistry;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.resource.InputStreamSource;
import com.networknt.schema.resource.SchemaLoader;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonSchemaFactoryUriCacheTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void cacheEnabled() throws JsonProcessingException {
        runCacheTest(true);
    }

    @Test
    void cacheDisabled() throws JsonProcessingException {
        runCacheTest(false);
    }

    private void runCacheTest(boolean enableCache) throws JsonProcessingException {
        CustomURIFetcher fetcher = new CustomURIFetcher();
        SchemaRegistry factory = buildJsonSchemaFactory(fetcher, enableCache);
        SchemaLocation schemaUri = SchemaLocation.of("cache:uri_mapping/schema1.json");
        String schema = "{ \"$schema\": \"https://json-schema.org/draft/2020-12/schema\", \"title\": \"json-object-with-schema\", \"type\": \"string\" }";
        fetcher.addResource(schemaUri.getAbsoluteIri(), schema);
        assertEquals(objectMapper.readTree(schema), factory.getSchema(schemaUri).schemaNode);

        String modifiedSchema = "{ \"$schema\": \"https://json-schema.org/draft/2020-12/schema\", \"title\": \"json-object-with-schema\", \"type\": \"object\" }";
        fetcher.addResource(schemaUri.getAbsoluteIri(), modifiedSchema);

        assertEquals(objectMapper.readTree(enableCache ? schema : modifiedSchema), factory.getSchema(schemaUri).schemaNode);
    }

    private SchemaRegistry buildJsonSchemaFactory(CustomURIFetcher uriFetcher, boolean enableSchemaCache) {
        return SchemaRegistry.builder(SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12))
                .enableSchemaCache(enableSchemaCache)
                .schemaLoaders(schemaLoaders -> schemaLoaders.add(uriFetcher))
                .dialectRegistry(new BasicDialectRegistry(Dialects.getDraft202012()))
                .build();
    }

    private class CustomURIFetcher implements SchemaLoader {

        private final Map<AbsoluteIri, InputStream> uriToResource = new HashMap<>();

        void addResource(AbsoluteIri uri, String schema) {
            addResource(uri, new ByteArrayInputStream(schema.getBytes(StandardCharsets.UTF_8)));
        }

        void addResource(AbsoluteIri uri, InputStream is) {
            uriToResource.put(uri, is);
        }

        @Override
        public InputStreamSource getSchema(AbsoluteIri absoluteIri) {
            return () -> uriToResource.get(absoluteIri);
        }
    }
}
