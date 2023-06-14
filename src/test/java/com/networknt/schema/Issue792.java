package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class Issue792 {

    @Test
    void test() throws JsonProcessingException {
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

        String schemaDef =
                "{\n" +
                "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "  \"$id\": \"http://some-id/\",\n" +
                "  \"title\": \"title\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"field\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"pattern\": \"^[A-Z]{2}$\"\n" +
                "    }\n" +
                "   }\n" +
                "}";

        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setTypeLoose(false);
        config.setFailFast(true);

        JsonSchema jsonSchema = schemaFactory.getSchema(schemaDef, config);
        JsonNode jsonNode = new ObjectMapper().readTree("{\"field\": \"pattern-violation\"}");

        //this works with 1.0.81, but not with 1.0.82+
        assertThrows(JsonSchemaException.class, () -> jsonSchema.validate(jsonNode));
    }
}
