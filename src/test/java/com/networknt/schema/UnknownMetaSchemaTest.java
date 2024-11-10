package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

class UnknownMetaSchemaTest {

    private final String schema1 = "{\"$schema\":\"http://json-schema.org/draft-07/schema\",\"title\":\"thingModel\",\"description\":\"description of thing\",\"type\":\"object\",\"properties\":{\"data\":{\"type\":\"integer\"},\"required\":[\"data\"]}}";
    private final String schema2 = "{\"$schema\":\"https://json-schema.org/draft-07/schema\",\"title\":\"thingModel\",\"description\":\"description of thing\",\"type\":\"object\",\"properties\":{\"data\":{\"type\":\"integer\"},\"required\":[\"data\"]}}";
    private final String schema3 = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"thingModel\",\"description\":\"description of thing\",\"type\":\"object\",\"properties\":{\"data\":{\"type\":\"integer\"},\"required\":[\"data\"]}}";

    private final String json = "{\"data\":1}";

    @Test
    void testSchema1() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(this.json);

        JsonSchemaFactory factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).build();
        JsonSchema jsonSchema = factory.getSchema(schema1);

        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        for(ValidationMessage error:errors) {
            System.out.println(error.getMessage());
        }
    }

    @Test
    void testSchema2() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(this.json);

        JsonSchemaFactory factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).build();
        JsonSchema jsonSchema = factory.getSchema(schema2);

        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        for(ValidationMessage error:errors) {
            System.out.println(error.getMessage());
        }
    }
    @Test
    void testSchema3() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(this.json);

        JsonSchemaFactory factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).build();
        JsonSchema jsonSchema = factory.getSchema(schema3);

        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        for(ValidationMessage error:errors) {
            System.out.println(error.getMessage());
        }
    }

    @Test
    void testNormalize() throws JsonSchemaException {

        String uri01 = "http://json-schema.org/draft-07/schema";
        String uri02 = "http://json-schema.org/draft-07/schema#";
        String uri03 = "http://json-schema.org/draft-07/schema?key=value";
        String uri04 = "http://json-schema.org/draft-07/schema?key=value&key2=value2";
        String expected = SchemaId.V7;

        Assertions.assertEquals(expected, JsonSchemaFactory.normalizeMetaSchemaUri(uri01));
        Assertions.assertEquals(expected, JsonSchemaFactory.normalizeMetaSchemaUri(uri02));
        Assertions.assertEquals(expected, JsonSchemaFactory.normalizeMetaSchemaUri(uri03));
        Assertions.assertEquals(expected, JsonSchemaFactory.normalizeMetaSchemaUri(uri04));

    }
}
