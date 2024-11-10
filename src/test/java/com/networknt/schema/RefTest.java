package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

class RefTest {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();
    
    @Test
    void shouldLoadRelativeClasspathReference() throws JsonProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(SchemaLocation.of("classpath:///schema/ref-main.json"), config);
        String input = "{\r\n"
                + "  \"DriverProperties\": {\r\n"
                + "    \"CommonProperties\": {\r\n"
                + "      \"field2\": \"abc-def-xyz\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        assertEquals(SchemaId.V4, schema.getValidationContext().getMetaSchema().getIri());
        Set<ValidationMessage> errors = schema.validate(OBJECT_MAPPER.readTree(input));
        assertEquals(1, errors.size());
        ValidationMessage error = errors.iterator().next();
        assertEquals("classpath:///schema/ref-ref.json#/definitions/DriverProperties/required",
                error.getSchemaLocation().toString());
        assertEquals("/properties/DriverProperties/properties/CommonProperties/$ref/required",
                error.getEvaluationPath().toString());
        assertEquals("field1", error.getProperty());
    }
    
    @Test
    void shouldLoadSchemaResource() throws JsonProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(SchemaLocation.of("classpath:///schema/ref-main-schema-resource.json"), config);
        String input = "{\r\n"
                + "  \"DriverProperties\": {\r\n"
                + "    \"CommonProperties\": {\r\n"
                + "      \"field2\": \"abc-def-xyz\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        assertEquals(SchemaId.V4, schema.getValidationContext().getMetaSchema().getIri());
        Set<ValidationMessage> errors = schema.validate(OBJECT_MAPPER.readTree(input));
        assertEquals(1, errors.size());
        ValidationMessage error = errors.iterator().next();
        assertEquals("https://www.example.org/common#/definitions/DriverProperties/required",
                error.getSchemaLocation().toString());
        assertEquals("/properties/DriverProperties/properties/CommonProperties/$ref/required",
                error.getEvaluationPath().toString());
        assertEquals("field1", error.getProperty());
        JsonSchema driver = schema.getValidationContext().getSchemaResources().get("https://www.example.org/driver#");
        JsonSchema common = schema.getValidationContext().getSchemaResources().get("https://www.example.org/common#");
        assertEquals(SchemaId.V4, driver.getValidationContext().getMetaSchema().getIri());
        assertEquals(SchemaId.V7, common.getValidationContext().getMetaSchema().getIri());

    }
}
