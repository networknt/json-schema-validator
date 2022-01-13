package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.util.Set;

public class Issue470Test {

    private static JsonSchema schema;

    @BeforeAll
    static void init() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        String schemaPath = "/schema/issue470-v7.json";
        InputStream schemaInputStream = Issue470Test.class.getResourceAsStream(schemaPath);
        schema = factory.getSchema(schemaInputStream);
    }

    private JsonNode getJsonNodeFromJsonData(String jsonFilePath) throws Exception {
        InputStream content = getClass().getResourceAsStream(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    @DisplayName("Test valid oneOf option 1")
    public void testValidJson1() throws Exception {
        JsonNode node = getJsonNodeFromJsonData("/data/issue470-valid-1.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test valid oneOf option 2")
    public void testValidJson2() throws Exception {
        JsonNode node = getJsonNodeFromJsonData("/data/issue470-valid-2.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test invalid oneOf option 1 - wrong type")
    @Disabled
    public void testInvalidJson1() throws Exception {
        JsonNode node = getJsonNodeFromJsonData("/data/issue470-invalid-1.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("$.search.byName.name: integer found, string expected", errors.iterator().next().getMessage());
    }

    @Test
    @Disabled
    @DisplayName("Test invalid oneOf option 1 - invalid value")
    public void testInvalidJson2() throws Exception {
        JsonNode node = getJsonNodeFromJsonData("/data/issue470-invalid-2.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("$.search.byName.name: may only be 20 characters long", errors.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test invalid oneOf option 2 - wrong type")
    @Disabled
    public void testInvalidJson3() throws Exception {
        JsonNode node = getJsonNodeFromJsonData("/data/issue470-invalid-3.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("$.search.byAge.age: string found, integer expected", errors.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Test invalid oneOf option 2 - invalid value")
    @Disabled
    public void testInvalidJson4() throws Exception {
        JsonNode node = getJsonNodeFromJsonData("/data/issue470-invalid-4.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("$.search.byAge.age: must have a maximum value of 150", errors.iterator().next().getMessage());
    }
}
