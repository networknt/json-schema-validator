package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * Quick start test.
 */
class QuickStartTest {
    @Test
    void addressExample() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://example.com/address.schema.json\",\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"description\": \"An address similar to http://microformats.org/wiki/h-card\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"postOfficeBox\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"extendedAddress\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"streetAddress\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"locality\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"region\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"postalCode\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"countryName\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"required\": [ \"locality\", \"region\", \"countryName\" ],\r\n"
                + "  \"dependentRequired\": {\r\n"
                + "    \"postOfficeBox\": [ \"streetAddress\" ],\r\n"
                + "    \"extendedAddress\": [ \"streetAddress\" ]\r\n"
                + "  }\r\n"
                + "}\r\n"
                + "\r\n"
                + "";
        String instanceData = "{\r\n"
                + "  \"postOfficeBox\": \"123\",\r\n"
                + "  \"streetAddress\": \"456 Main St\",\r\n"
                + "  \"locality\": \"Cityville\",\r\n"
                + "  \"region\": \"State\",\r\n"
                + "  \"postalCode\": \"12345\",\r\n"
                + "  \"countryName\": \"Country\"\r\n"
                + "}";
        Map<String, String> schemas = new HashMap<>();
        schemas.put("https://example.com/address.schema.json", schemaData);
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(),
                builder -> builder.schemas(schemas));
        Schema schema = schemaRegistry.getSchema(SchemaLocation.of("https://example.com/address.schema.json"));
        List<Error> errors = schema.validate(instanceData, InputFormat.JSON);
        assertEquals(0, errors.size());

        String invalidInstanceData = "{\r\n"
                + "  \"postOfficeBox\": \"123\",\r\n"
                + "  \"streetAddress\": \"456 Main St\",\r\n"
                + "  \"region\": \"State\",\r\n"
                + "  \"postalCode\": \"12345\",\r\n"
                + "  \"countryName\": \"Country\"\r\n"
                + "}";
        errors = schema.validate(invalidInstanceData, InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals("required", errors.get(0).getKeyword());
    }

    @Test
    void schemaFromSchemaLocationMapping() {
        SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaIdResolvers(schemaIdResolvers -> schemaIdResolvers
                        .mapPrefix("https://www.example.com/schema", "classpath:schema")));
        /*
         * This should be cached for performance.
         */
        Schema schemaFromSchemaLocation = schemaRegistry
                .getSchema(SchemaLocation.of("https://www.example.com/schema/example-ref.json"));
        /*
         * By default all schemas are preloaded eagerly but ref resolve failures are not
         * thrown. You check if there are issues with ref resolving using
         * initializeValidators()
         */
        schemaFromSchemaLocation.initializeValidators();
        List<Error> errors = schemaFromSchemaLocation.validate("{\"id\": \"2\"}", InputFormat.JSON,
                executionContext -> executionContext
                        .executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertEquals(1, errors.size());
    }

    @Test
    void schemaFromSchemaLocationContent() {
        String schemaData = "{\"enum\":[1, 2, 3, 4]}";

        SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemas(
                        Collections.singletonMap("https://www.example.com/schema/example-ref.json", schemaData)));
        /*
         * This should be cached for performance.
         */
        Schema schemaFromSchemaLocation = schemaRegistry
                .getSchema(SchemaLocation.of("https://www.example.com/schema/example-ref.json"));
        /*
         * By default all schemas are preloaded eagerly but ref resolve failures are not
         * thrown. You check if there are issues with ref resolving using
         * initializeValidators()
         */
        schemaFromSchemaLocation.initializeValidators();
        List<Error> errors = schemaFromSchemaLocation.validate("{\"id\": \"2\"}", InputFormat.JSON,
                executionContext -> executionContext
                        .executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertEquals(1, errors.size());
    }

    @Test
    void schemaFromClasspath() {
        SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        /*
         * This should be cached for performance.
         * 
         * Loading from using the retrieval IRI is not recommended as it may cause
         * confusing when resolving relative $ref when $id is also used.
         */
        Schema schemaFromClasspath = schemaRegistry.getSchema(SchemaLocation.of("classpath:schema/example-ref.json"));
        /*
         * By default all schemas are preloaded eagerly but ref resolve failures are not
         * thrown. You check if there are issues with ref resolving using
         * initializeValidators()
         */
        schemaFromClasspath.initializeValidators();
        List<Error> errors = schemaFromClasspath.validate("{\"id\": \"2\"}", InputFormat.JSON,
                executionContext -> executionContext
                        .executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertEquals(1, errors.size());
    }

    @Test
    void schemaFromString() {
        SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        /*
         * This should be cached for performance.
         * 
         * Loading from a String is not recommended as there is no base IRI to use for
         * resolving relative $ref.
         */
        Schema schemaFromString = schemaRegistry.getSchema("{\"enum\":[1, 2, 3, 4]}");
        List<Error> errors = schemaFromString.validate("7", InputFormat.JSON, executionContext -> executionContext
                .executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertEquals(1, errors.size());
    }

    @Test
    void schemaFromJsonNode() throws JsonProcessingException {
        SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        JsonNode schemaNode = JsonMapperFactory.getInstance().readTree(
                "{\"$schema\": \"http://json-schema.org/draft-06/schema#\", \"properties\": { \"id\": {\"type\": \"number\"}}}");
        /*
         * This should be cached for performance.
         * 
         * Loading from a JsonNode is not recommended as there is no base IRI to use for
         * resolving relative $ref.
         *
         * Note that the V202012 from the schemaRegistry is the default version if $schema is
         * not specified. As $schema is specified in the data, V6 is used.
         */
        Schema schemaFromNode = schemaRegistry.getSchema(schemaNode);
        /*
         * By default all schemas are preloaded eagerly but ref resolve failures are not
         * thrown. You check if there are issues with ref resolving using
         * initializeValidators()
         */
        schemaFromNode.initializeValidators();
        List<Error> errors = schemaFromNode.validate("{\"id\": \"2\"}", InputFormat.JSON,
                executionContext -> executionContext
                        .executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertEquals(1, errors.size());
    }
}
