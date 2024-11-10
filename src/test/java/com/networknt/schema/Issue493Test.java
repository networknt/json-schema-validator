package com.networknt.schema;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class Issue493Test
{

    private static final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
    private static final String schemaPath1 = "/schema/issue493.json";

    private JsonNode getJsonNodeFromJsonData (String jsonFilePath)
            throws Exception
    {
        InputStream content = getClass().getResourceAsStream(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    @DisplayName("Test valid with required item only")
    void testValidJson1 ()
            throws Exception
    {
        InputStream schemaInputStream = Issue493Test.class.getResourceAsStream(schemaPath1);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue493-valid-1.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test valid with optional item")
    void testValidJson2 ()
            throws Exception
    {
        InputStream schemaInputStream = Issue493Test.class.getResourceAsStream(schemaPath1);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue493-valid-2.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test invalid with required item but wrong type")
    void testInvalidJson1 ()
            throws Exception
    {
        InputStream schemaInputStream = Issue493Test.class.getResourceAsStream(schemaPath1);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue493-invalid-1.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(2, errors.size());

        Set<String> allErrorMessages = new HashSet<>();
        errors.forEach(vm -> {
            allErrorMessages.add(vm.getMessage());
        });
        assertThat(allErrorMessages,
                   Matchers.containsInAnyOrder("$.parameters[0].value: string found, integer expected",
                                               "$.parameters[0].value: does not match the regex pattern ^\\{\\{.+\\}\\}$"));
    }

    @Test
    @DisplayName("Test invalid with optional item but wrong type")
    void testInvalidJson2 ()
            throws Exception
    {
        InputStream schemaInputStream = Issue493Test.class.getResourceAsStream(schemaPath1);
        JsonSchema schema = factory.getSchema(schemaInputStream);
        JsonNode node = getJsonNodeFromJsonData("/data/issue493-invalid-2.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(3, errors.size());

        Set<String> allErrorMessages = new HashSet<>();
        errors.forEach(vm -> {
            allErrorMessages.add(vm.getMessage());
        });
        assertThat(allErrorMessages, Matchers.containsInAnyOrder(
            "$.parameters[1].value: string found, integer expected",
            "$.parameters[1].value: does not match the regex pattern ^\\{\\{.+\\}\\}$",
            "$.parameters[1]: must be valid to one and only one schema, but 0 are valid"
        ));
    }
}
