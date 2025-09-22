package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.dialect.DialectId;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class UnknownMetaSchemaTest {

    private final String schema1 = "{\"$schema\":\"http://json-schema.org/draft-07/schema\",\"title\":\"thingModel\",\"description\":\"description of thing\",\"type\":\"object\",\"properties\":{\"data\":{\"type\":\"integer\"},\"required\":[\"data\"]}}";
    private final String schema2 = "{\"$schema\":\"https://json-schema.org/draft-07/schema\",\"title\":\"thingModel\",\"description\":\"description of thing\",\"type\":\"object\",\"properties\":{\"data\":{\"type\":\"integer\"},\"required\":[\"data\"]}}";
    private final String schema3 = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"thingModel\",\"description\":\"description of thing\",\"type\":\"object\",\"properties\":{\"data\":{\"type\":\"integer\"},\"required\":[\"data\"]}}";

    private final String json = "{\"data\":1}";

    @Test
    void testSchema1() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(this.json);

        SchemaRegistry factory = SchemaRegistry.builder(SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_7)).build();
        Schema jsonSchema = factory.getSchema(schema1);

        List<Error> errors = jsonSchema.validate(jsonNode);
        for(Error error:errors) {
            System.out.println(error.getMessage());
        }
    }

    @Test
    void testSchema2() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(this.json);

        SchemaRegistry factory = SchemaRegistry.builder(SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_7)).build();
        Schema jsonSchema = factory.getSchema(schema2);

        List<Error> errors = jsonSchema.validate(jsonNode);
        for(Error error:errors) {
            System.out.println(error.getMessage());
        }
    }
    @Test
    void testSchema3() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(this.json);

        SchemaRegistry factory = SchemaRegistry.builder(SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_7)).build();
        Schema jsonSchema = factory.getSchema(schema3);

        List<Error> errors = jsonSchema.validate(jsonNode);
        for(Error error:errors) {
            System.out.println(error.getMessage());
        }
    }

    @Test
    void testNormalize() throws JsonSchemaException {

        String uri01 = "http://json-schema.org/draft-07/schema";
        String uri02 = "http://json-schema.org/draft-07/schema#";
        String uri03 = "http://json-schema.org/draft-07/schema?key=value";
        String uri04 = "http://json-schema.org/draft-07/schema?key=value&key2=value2";
        String expected = DialectId.DRAFT_7;

        Assertions.assertEquals(expected, SchemaRegistry.normalizeDialectId(uri01));
        Assertions.assertEquals(expected, SchemaRegistry.normalizeDialectId(uri02));
        Assertions.assertEquals(expected, SchemaRegistry.normalizeDialectId(uri03));
        Assertions.assertEquals(expected, SchemaRegistry.normalizeDialectId(uri04));

    }
}
