package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.uri.URITranslator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue285Test {
    private ObjectMapper mapper = new ObjectMapper();
    private JsonSchemaFactory schemaFactory = JsonSchemaFactory
		.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909))
		.objectMapper(mapper)
		.addUriTranslator(URITranslator.combine(
			URITranslator.prefix("http://json-schema.org", "resource:"),
			URITranslator.prefix("https://json-schema.org", "resource:")
		))
		.build();


    String schemaStr = "{\n" +
            "  \"$id\": \"https://example.com/person.schema.json\",\n" +
            "  \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\n" +
            "  \"title\": \"Person\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"additionalProperties\": false,\n" +
            "  \"properties\": {\n" +
            "    \"name\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"properties\": {\n" +
            "        \"firstName\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"minLength\": 3,\n" +
            "          \"description\": \"The person's first name.\"\n" +
            "        },\n" +
            "        \"lastName\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"description\": \"The person's last name.\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    String person = "{\n" +
            "  \"name\": {\n" +
            "    \"firstName\": \"John\",\n" +
            "    \"lastName\": true\n" +
            "  }\n" +
            "}\n";

    // This checks the that the validation checks the type of the nested attribute.
    // In this case the "lastName" should be a string.
    // The result is as expected and we get an validation error.
    @Test
    public void nestedValidation() throws IOException {
        JsonSchema jsonSchema = schemaFactory.getSchema(schemaStr);
        Set<ValidationMessage> validationMessages = jsonSchema.validate(mapper.readTree(person));

        System.err.println("\n" + Arrays.toString(validationMessages.toArray()));

        assertFalse(validationMessages.isEmpty());


    }

    String invalidNestedSchema = "{\n" +
            "  \"$id\": \"https://example.com/person.schema.json\",\n" +
            "  \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\n" +
            "  \"title\": \"Person\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"additionalProperties\": false,\n" +
            "  \"properties\": {\n" +
            "    \"name\": {\n" +
            "      \"type\": \"foo\",\n" +
            "      \"additionalProperties\": false,\n" +
            "      \"properties\": {\n" +
            "        \"firstName\": {\n" +
            "          \"type\": \"string\",\n" +
            "          \"minLength\": 3,\n" +
            "          \"description\": \"The person's first name.\"\n" +
            "        },\n" +
            "        \"lastName\": {\n" +
            "          \"type\": \"foo\",\n" +
            "          \"description\": \"The person's last name.\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    // This checks the that the validation checks the type of the nested attribute.
    // Based on the meta-schema found on https://json-schema.org/draft/2019-09/schema.
    // In this case a nested type declaration isn't valid and should raise an error.
    // The result is not as expected and we get no validation error.
    @Test
    @Disabled
    public void nestedTypeValidation() throws IOException, URISyntaxException {
        URI uri = new URI("https://json-schema.org/draft/2019-09/schema");
        JsonSchema jsonSchema = schemaFactory.getSchema(uri);
        Set<ValidationMessage> validationMessages = jsonSchema.validate(mapper.readTree(invalidNestedSchema));

        System.err.println("\n" + Arrays.toString(validationMessages.toArray()));

        assertFalse(validationMessages.isEmpty());
    }

    String invalidSchema = "{\n" +
            "  \"$id\": \"https://example.com/person.schema.json\",\n" +
            "  \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\n" +
            "  \"title\": \"Person\",\n" +
            "  \"type\": \"foo\",\n" +
            "  \"additionalProperties\": false\n" +
            "}";

    // This checks the that the validation checks the type of the JSON.
    // Based on the meta-schema found on https://json-schema.org/draft/2019-09/schema.
    // In this case the toplevel type declaration isn't valid and should raise an error.
    // The result is as expected and we get no validation error: '[$.type: does not have a value in the enumeration [array, boolean, integer, null, number, object, string], $.type: should be valid to any of the schemas array]'.
    @Test
    public void typeValidation() throws IOException, URISyntaxException {
        URI uri = new URI("https://json-schema.org/draft/2019-09/schema");
        JsonSchema jsonSchema = schemaFactory.getSchema(uri);
        Set<ValidationMessage> validationMessages = jsonSchema.validate(mapper.readTree(invalidSchema));

        System.err.println("\n" + Arrays.toString(validationMessages.toArray()));

        assertFalse(validationMessages.isEmpty());
    }
}
