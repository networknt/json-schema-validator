package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.networknt.schema.dialect.DialectId;

class RefTest {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();
    
    @Test
    void shouldLoadRelativeClasspathReference() throws JsonProcessingException {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(SchemaLocation.of("classpath:///schema/ref-main.json"));
        String input = "{\r\n"
                + "  \"DriverProperties\": {\r\n"
                + "    \"CommonProperties\": {\r\n"
                + "      \"field2\": \"abc-def-xyz\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        assertEquals(DialectId.DRAFT_4, schema.getSchemaContext().getDialect().getId());
        List<Error> errors = schema.validate(OBJECT_MAPPER.readTree(input));
        assertEquals(1, errors.size());
        Error error = errors.iterator().next();
        assertEquals("classpath:///schema/ref-ref.json#/definitions/DriverProperties/required",
                error.getSchemaLocation().toString());
        assertEquals("/properties/DriverProperties/properties/CommonProperties/$ref/required",
                error.getEvaluationPath().toString());
        assertEquals("field1", error.getProperty());
    }
    
    @Test
    void shouldLoadSchemaResource() throws JsonProcessingException {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(SchemaLocation.of("classpath:///schema/ref-main-schema-resource.json"));
        String input = "{\r\n"
                + "  \"DriverProperties\": {\r\n"
                + "    \"CommonProperties\": {\r\n"
                + "      \"field2\": \"abc-def-xyz\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        assertEquals(DialectId.DRAFT_4, schema.getSchemaContext().getDialect().getId());
        List<Error> errors = schema.validate(OBJECT_MAPPER.readTree(input));
        assertEquals(1, errors.size());
        Error error = errors.iterator().next();
        assertEquals("https://www.example.org/common#/definitions/DriverProperties/required",
                error.getSchemaLocation().toString());
        assertEquals("/properties/DriverProperties/properties/CommonProperties/$ref/required",
                error.getEvaluationPath().toString());
        assertEquals("field1", error.getProperty());
        Schema driver = schema.getSchemaContext().getSchemaResources().get("https://www.example.org/driver#");
        Schema common = schema.getSchemaContext().getSchemaResources().get("https://www.example.org/common#");
        assertEquals(DialectId.DRAFT_4, driver.getSchemaContext().getDialect().getId());
        assertEquals(DialectId.DRAFT_7, common.getSchemaContext().getDialect().getId());

    }
}
