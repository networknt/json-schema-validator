package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Set;

public class UnknownMetaSchemaTest {

    private String schema =
            "{\n" +
            "  \"$schema\": \"https://json-schema.org/draft-07/schema\",\n" +
            "  \"title\": \"thingModel\",\n" +
            "  \"description\": \"description of thing\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"data\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"data\"\n" +
            "  ]\n" +
            "}";

    private String json =
            "{\n" +
            "    \"data\": 1\n" +
            "}";


    @Test
    public void test() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(this.json);

        JsonSchemaFactory factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).objectMapper(mapper).build();
        JsonSchema jsonSchema = factory.getSchema(schema);

        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        for(ValidationMessage error:errors) {
            System.out.println(error.getMessage());
        }
    }
}
