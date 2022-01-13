package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Stream;

class Issue491Test {

    private static JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private static String schemaPath1 = "/schema/issue491-v7.json";
    private static String schemaPath2 = "/schema/issue491_2-v7.json";
    private static String schemaPath3 = "/schema/issue491_3-v7.json";

    private JsonNode getJsonNodeFromJsonData(String jsonFilePath) throws Exception {
        InputStream content = getClass().getResourceAsStream(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    @DisplayName("Test valid oneOf option 1")
    void testValidJson1() throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath1);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue491-valid-1.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test valid oneOf option 2")
    void testValidJson2() throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath1);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue491-valid-2.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test valid oneOf option 1")
    void testValidJson3() throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath2);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue491-valid-3.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test valid oneOf option 2")
    void testValidJson4() throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath2);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue491-valid-2.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test valid oneOf option 1")
    void testValidJson5() throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath3);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue491-valid-4.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test valid oneOf option 2")
    void testValidJson6() throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath3);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue491-valid-2.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Disabled
    @DisplayName("Test invalid oneOf option 1 - wrong type")
    void testInvalidJson1() throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath1);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue491-invalid-1.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(3, errors.size());
        Assertions.assertEquals("$.search.searchAge.age: string found, integer expected", errors.iterator().next().getMessage());
    }

    @Disabled
    @DisplayName("Test invalid oneOf option 2 - wrong type")
    void testInvalidJson2() throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath1);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue491-invalid-2.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(3, errors.size());
        Assertions.assertEquals("$.search.name: integer found, string expected", errors.iterator().next().getMessage());
    }

    @Disabled
    @DisplayName("Test invalid oneOf option 1 - wrong type")
    void testInvalidJson3() throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath2);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue491-invalid-3.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(3, errors.size());
        Assertions.assertEquals("$.search.byAge.age: string found, integer expected", errors.iterator().next().getMessage());
    }

    @Disabled
    @DisplayName("Test invalid oneOf option 2 - wrong type")
    void testInvalidJson4() throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath2);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue491-invalid-2.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(3, errors.size());
        Assertions.assertEquals("$.search.name: integer found, string expected", errors.iterator().next().getMessage());
    }

    @ParameterizedTest
    @MethodSource("parametersProvider")
    @Disabled
    @DisplayName("Test invalid oneOf option - wrong types or values")
    void testInvalidJson5(String jsonPath, String expectedError) throws Exception {
        InputStream schemaInputStream = Issue491Test.class.getResourceAsStream(schemaPath3);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData(jsonPath);
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(3, errors.size());
        Assertions.assertEquals(expectedError, errors.iterator().next().getMessage());
    }

    private static Stream<Arguments> parametersProvider() {
        return Stream.of(
                Arguments.of("/data/issue491-invalid-4.json", "$.search.age: string found, integer expected"),
                Arguments.of("/data/issue491-invalid-2.json", "$.search.name: integer found, string expected"),
                Arguments.of("/data/issue491-invalid-5.json", "$.search.age: must have a maximum value of 150"),
                Arguments.of("/data/issue491-invalid-6.json", "$.search.name: may only be 20 characters long")
        );
    }
}
