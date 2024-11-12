package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * Sample test.
 */
class SampleTest {
    @Test
    void schemaFromSchemaLocationMapping() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.schemaMappers(
                schemaMappers -> schemaMappers.mapPrefix("https://www.example.com/schema", "classpath:schema")));
        /*
         * This should be cached for performance.
         */
        JsonSchema schemaFromSchemaLocation = factory
                .getSchema(SchemaLocation.of("https://www.example.com/schema/example-ref.json"));
        /*
         * By default all schemas are preloaded eagerly but ref resolve failures are not
         * thrown. You check if there are issues with ref resolving using
         * initializeValidators()
         */
        schemaFromSchemaLocation.initializeValidators();
        Set<ValidationMessage> errors = schemaFromSchemaLocation.validate("{\"id\": \"2\"}", InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));
        assertEquals(1, errors.size());
    }

    @Test
    void schemaFromSchemaLocationContent() {
        String schemaData = "{\"enum\":[1, 2, 3, 4]}";
        
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(
                        Collections.singletonMap("https://www.example.com/schema/example-ref.json", schemaData))));
        /*
         * This should be cached for performance.
         */
        JsonSchema schemaFromSchemaLocation = factory
                .getSchema(SchemaLocation.of("https://www.example.com/schema/example-ref.json"));
        /*
         * By default all schemas are preloaded eagerly but ref resolve failures are not
         * thrown. You check if there are issues with ref resolving using
         * initializeValidators()
         */
        schemaFromSchemaLocation.initializeValidators();
        Set<ValidationMessage> errors = schemaFromSchemaLocation.validate("{\"id\": \"2\"}", InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));
        assertEquals(1, errors.size());
    }

    @Test
    void schemaFromClasspath() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        /*
         * This should be cached for performance.
         * 
         * Loading from using the retrieval IRI is not recommended as it may cause
         * confusing when resolving relative $ref when $id is also used.
         */
        JsonSchema schemaFromClasspath = factory.getSchema(SchemaLocation.of("classpath:schema/example-ref.json"));
        /*
         * By default all schemas are preloaded eagerly but ref resolve failures are not
         * thrown. You check if there are issues with ref resolving using
         * initializeValidators()
         */
        schemaFromClasspath.initializeValidators();
        Set<ValidationMessage> errors = schemaFromClasspath.validate("{\"id\": \"2\"}", InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));
        assertEquals(1, errors.size());
    }

    @Test
    void schemaFromString() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        /*
         * This should be cached for performance.
         * 
         * Loading from a String is not recommended as there is no base IRI to use for
         * resolving relative $ref.
         */
        JsonSchema schemaFromString = factory
                .getSchema("{\"enum\":[1, 2, 3, 4]}");
        Set<ValidationMessage> errors = schemaFromString.validate("7", InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));
        assertEquals(1, errors.size());
    }

    @Test
    void schemaFromJsonNode() throws JsonProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        JsonNode schemaNode = JsonMapperFactory.getInstance().readTree(
                "{\"$schema\": \"http://json-schema.org/draft-06/schema#\", \"properties\": { \"id\": {\"type\": \"number\"}}}");
        /*
         * This should be cached for performance.
         * 
         * Loading from a JsonNode is not recommended as there is no base IRI to use for
         * resolving relative $ref.
         *
         * Note that the V202012 from the factory is the default version if $schema is not
         * specified. As $schema is specified in the data, V6 is used.
         */
        JsonSchema schemaFromNode = factory.getSchema(schemaNode);
        /*
         * By default all schemas are preloaded eagerly but ref resolve failures are not
         * thrown. You check if there are issues with ref resolving using
         * initializeValidators()
         */
        schemaFromNode.initializeValidators();
        Set<ValidationMessage> errors = schemaFromNode.validate("{\"id\": \"2\"}", InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));
        assertEquals(1, errors.size());
    }
}
