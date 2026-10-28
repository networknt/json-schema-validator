package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Issue792Test {

    @Test
    void test() throws JsonProcessingException {
        JsonNode jsonNode = new ObjectMapper().readTree("{\"field\": \"pattern-violation\", \"other\": \"pattern-violation\"}");

        assertEquals(1, schema(true).validate(jsonNode).size());
        assertEquals(2, schema(false).validate(jsonNode).size());
    }

    private static Schema schema(boolean failFast) {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().typeLoose(false).failFast(failFast).build();

        SchemaRegistry schemaFactory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7, builder -> builder.schemaRegistryConfig(config));

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
                "    },\n" +
                "    \"other\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"pattern\": \"^[A-Z]{2}$\"\n" +
                "    }\n" +
                "   }\n" +
                "}";

        return schemaFactory.getSchema(schemaDef);
    }
}
