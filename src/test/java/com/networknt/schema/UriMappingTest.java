package com.networknt.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory.Builder;
import com.networknt.schema.uri.ClasspathURLFactory;
import com.networknt.schema.uri.URLFactory;

@RunWith(Parameterized.class)
public class UriMappingTest {
    @Parameterized.Parameters
    public static Collection<?> parameters() {
      return Arrays.asList(new Object[][] {
         { "draft4", JsonMetaSchema.getV4(), SpecVersion.VersionFlag.V4 },
         { "draft6", JsonMetaSchema.getV6(), SpecVersion.VersionFlag.V6 },
         { "draft7", JsonMetaSchema.getV7(), SpecVersion.VersionFlag.V7 },
         { "draft2019-09", JsonMetaSchema.getV201909(), SpecVersion.VersionFlag.V201909 }
      });
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final ClasspathURLFactory classpathURLFactory = new ClasspathURLFactory();
    private final URLFactory urlFactory = new URLFactory();

    private final String resourcePrefix;
    private final JsonMetaSchema metaSchema;
    private final SpecVersion.VersionFlag specVersion;
    
    // This constructor is where the parameters are injected by Junit.
    public UriMappingTest(
            final String resourcePrefix, 
            final JsonMetaSchema metaSchema, 
            final SpecVersion.VersionFlag specVersion) {
        super();
        this.resourcePrefix = resourcePrefix;
        this.metaSchema = metaSchema;
        this.specVersion = specVersion;
    }
    
    /**
     * Validate that a JSON URI Mapping file containing the URI Mapping schema is
     * schema valid.
     * 
     * @throws IOException if unable to parse the mapping file
     */
    @Test
    public void testBuilderUriMappingUri() throws IOException {
        URL mappings = ClasspathURLFactory.convert(this.classpathURLFactory.create(String.format(
                "resource:%s/uri_mapping/uri-mapping.json",
                this.resourcePrefix)));
        Builder builder = JsonSchemaFactory.builder()
                .defaultMetaSchemaURI(this.metaSchema.getUri())
                .addMetaSchema(this.metaSchema)
                .addUriMappings(getUriMappingsFromUrl(mappings));
        JsonSchemaFactory instance = builder.build();
        JsonSchema schema = instance.getSchema(this.urlFactory.create(String.format(
                "https://raw.githubusercontent.com/networknt/json-schema-validator/master/src/test/resources/%s/uri_mapping/uri-mapping.schema.json",
                this.resourcePrefix)));
        assertEquals(0, schema.validate(mapper.readTree(mappings)).size());
    }

    /**
     * Validate that local URI is used when attempting to get a schema that is not
     * available publicly. Use the URL http://example.com/invalid/schema/url to use
     * a public URL that returns a 404 Not Found. The locally mapped schema is a
     * valid, but empty schema.
     * 
     * @throws IOException if unable to parse the mapping file
     */
    @Test
    public void testBuilderExampleMappings() throws IOException {
        JsonSchemaFactory instance = JsonSchemaFactory.getInstance(this.specVersion);
        URI example = this.urlFactory.create("http://example.com/invalid/schema/url");
        // first test that attempting to use example URL throws an error
        try {
            JsonSchema schema = instance.getSchema(example);
            schema.validate(mapper.createObjectNode());
            fail("Expected exception not thrown");
        } catch (JsonSchemaException ex) {
            Throwable cause = ex.getCause();
            if (!(cause instanceof FileNotFoundException || cause instanceof UnknownHostException)) {
                fail("Unexpected cause for JsonSchemaException");
            }
            // passing, so do nothing
        } catch (Exception ex) {
            fail("Unexpected exception thrown");
        }
        URL mappings = ClasspathURLFactory.convert(this.classpathURLFactory.create(String.format(
                "resource:%s/uri_mapping/invalid-schema-uri.json",
                this.resourcePrefix)));
        Builder builder = JsonSchemaFactory.builder()
                .defaultMetaSchemaURI(this.metaSchema.getUri())
                .addMetaSchema(this.metaSchema)
                .addUriMappings(getUriMappingsFromUrl(mappings));
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
    public void testValidatorConfigUriMappingUri() throws IOException {
        JsonSchemaFactory instance = JsonSchemaFactory.getInstance(this.specVersion);
        URL mappings = ClasspathURLFactory.convert(this.classpathURLFactory.create(String.format(
                "resource:%s/uri_mapping/uri-mapping.json",
                this.resourcePrefix)));
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setUriMappings(getUriMappingsFromUrl(mappings));
        JsonSchema schema = instance.getSchema(this.urlFactory.create(
                String.format(
                        "https://raw.githubusercontent.com/networknt/json-schema-validator/master/src/test/resources/%s/uri_mapping/uri-mapping.schema.json",
                        this.resourcePrefix)),
                config);
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
    public void testValidatorConfigExampleMappings() throws IOException {
        JsonSchemaFactory instance = JsonSchemaFactory.getInstance(this.specVersion);
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        URI example = this.urlFactory.create("http://example.com/invalid/schema/url");
        // first test that attempting to use example URL throws an error
        try {
            JsonSchema schema = instance.getSchema(example, config);
            schema.validate(mapper.createObjectNode());
            fail("Expected exception not thrown");
        } catch (JsonSchemaException ex) {
            Throwable cause = ex.getCause();
            if (!(cause instanceof FileNotFoundException || cause instanceof UnknownHostException)) {
                fail("Unexpected cause for JsonSchemaException");
            }
            // passing, so do nothing
        } catch (Exception ex) {
            fail("Unexpected exception thrown");
        }
        URL mappings = ClasspathURLFactory.convert(this.classpathURLFactory.create(String.format(
                "resource:%s/uri_mapping/invalid-schema-uri.json",
                this.resourcePrefix)));
        config.setUriMappings(getUriMappingsFromUrl(mappings));
        JsonSchema schema = instance.getSchema(example, config);
        assertEquals(0, schema.validate(mapper.createObjectNode()).size());
    }

    @Test
    public void testMappingsForRef() throws IOException {
        JsonSchemaFactory instance = JsonSchemaFactory.getInstance(this.specVersion);
        URL mappings = ClasspathURLFactory.convert(this.classpathURLFactory.create(String.format(
                "resource:%s/uri_mapping/schema-with-ref-mapping.json",
                this.resourcePrefix)));
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setUriMappings(getUriMappingsFromUrl(mappings));
        JsonSchema schema = instance.getSchema(
                this.classpathURLFactory.create(String.format(
                        "resource:%s/uri_mapping/schema-with-ref.json",
                        this.resourcePrefix)),
                config);
        assertEquals(0, schema.validate(mapper.readTree("[]")).size());
    }

    private Map<String, String> getUriMappingsFromUrl(URL url) throws MalformedURLException, IOException {
        HashMap<String, String> map = new HashMap<String, String>();
        for (JsonNode mapping : mapper.readTree(url)) {
            map.put(mapping.get("publicURL").asText(),
                    mapping.get("localURL").asText());
        }
        return map;
    }
}
