package com.networknt.schema;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.url.URLFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UrlMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Validate that a JSON URL Mapping file containing the URL Mapping schema
     * is schema valid.
     * 
     * @throws IOException if unable to parse the mapping file
     */
    @Test
    public void testUrlMappingUrl() throws IOException {
        JsonSchemaFactory instance = JsonSchemaFactory.getInstance();
        URL mappings = URLFactory.toURL("resource:tests/url_mapping/url-mapping.json");
        instance.addUrlMappings(mappings);
        JsonSchema schema = instance.getSchema(new URL("https://raw.githubusercontent.com/networknt/json-schema-validator/master/src/main/resources/url-mapping.schema.json"));
        assertEquals(0, schema.validate(mapper.readTree(mappings)).size());
    }

    /**
     * Validate that local URL is used when attempting to get a schema that is not
     * available publicly. Use the URL http://example.com/invalid/schema/url to use
     * a public URL that returns a 404 Not Found. The locally mapped schema is a
     * valid, but empty schema.
     * 
     * @throws IOException if unable to parse the mapping file
     */
    @Test
    public void testExampleMappings() throws IOException {
        JsonSchemaFactory instance = JsonSchemaFactory.getInstance();
        URL example = new URL("http://example.com/invalid/schema/url");
        // first test that attempting to use example URL throws an error
        try {
            JsonSchema schema = instance.getSchema(example);
            schema.validate(mapper.createObjectNode());
            fail("Expected exception not thrown");
        } catch (JsonSchemaException ex) {
            Throwable cause = ex.getCause();
            if (!(cause instanceof FileNotFoundException ||
                    cause instanceof UnknownHostException)) {
                fail("Unexpected cause for JsonSchemaException");
            }
            // passing, so do nothing
        } catch (Exception ex) {
            fail("Unexpected exception thrown");
        }
        URL mappings = URLFactory.toURL("resource:tests/url_mapping/invalid-schema-url.json");
        instance.addUrlMappings(mappings);
        JsonSchema schema = instance.getSchema(example);
        assertEquals(0, schema.validate(mapper.createObjectNode()).size());
    }
}
