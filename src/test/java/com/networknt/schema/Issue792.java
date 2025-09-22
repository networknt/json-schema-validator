package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Issue792 {

    @Test
    void test() throws JsonProcessingException {
        SchemaRegistry schemaFactory = SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_7);

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

        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().typeLoose(false).failFast(true).build();

        Schema jsonSchema = schemaFactory.getSchema(schemaDef, config);
        JsonNode jsonNode = new ObjectMapper().readTree("{\"field\": \"pattern-violation\"}");

        assertEquals(1, jsonSchema.validate(jsonNode).size());
    }
}
