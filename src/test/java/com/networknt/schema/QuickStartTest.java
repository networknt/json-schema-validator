package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.output.OutputUnit;
import com.networknt.schema.regex.JoniRegularExpressionFactory;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.utils.JsonNodes;

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
    void readme() {
        /*
         * The SchemaRegistryConfig can be optionally used to configure certain aspects
         * of how the validation is performed.
         * 
         * By default the JDK regular expression implementation which is not ECMA 262
         * compliant is used. The GraalJSRegularExpressionFactory.getInstance() offers
         * the best compliance followed by JoniRegularExpressionFactory.getInstance()
         * but both require additional optional dependencies.
         */
        SchemaRegistryConfig schemaRegistryConfig = SchemaRegistryConfig.builder()
                .regularExpressionFactory(JoniRegularExpressionFactory.getInstance()).build();

        /*
         * This creates a schema registry that supports all the standard dialects for
         * cross-dialect validation and will use Draft 2020-12 as the default if $schema
         * is not specified in the schema data. If $schema is specified in the schema
         * data then that schema dialect will be used instead and this version is
         * ignored.
         */
        SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(schemaRegistryConfig)
                        /*
                         * This creates a mapping from $id which starts with
                         * https://www.example.org/schema to the retrieval IRI classpath:schema.
                         */
                        .schemaIdResolvers(schemaIdResolvers -> schemaIdResolvers
                                .mapPrefix("https://www.example.com/schema", "classpath:schema")));

        /*
         * Due to the mapping the schema will be retrieved from the classpath at
         * classpath:schema/example-main.json. If the schema data does not specify an
         * $id the absolute IRI of the schema location will be used as the $id. If the
         * schema data does not specify a dialect using $schema the default dialect
         * specified when creating the schema registry.
         */
        Schema schema = schemaRegistry.getSchema(SchemaLocation.of("https://www.example.com/schema/example-main.json"));
        String input = "{\r\n"
         + "  \"main\": {\r\n"
         + "    \"common\": {\r\n"
         + "      \"field\": \"invalidfield\"\r\n"
         + "    }\r\n"
         + "  }\r\n"
         + "}";

        List<Error> errors = schema.validate(input, InputFormat.JSON, executionContext -> {
            /*
             * By default since Draft 2019-09 the format keyword only generates annotations
             * and not assertions.
             */
            executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
        });
        assertEquals(2, errors.size());
    }

    @Test
    void readmeMetaSchema() {
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
        /*
         * Due to the mapping the meta-schema for the dialect will be retrieved from the
         * classpath at classpath:draft/2020-12/schema.
         */
        Schema schema = schemaRegistry.getSchema(SchemaLocation.of(Dialects.getDraft202012().getId()));
        String input = "{\r\n"
            + "  \"type\": \"object\",\r\n"
            + "  \"properties\": {\r\n"
            + "    \"key\": {\r\n"
            + "      \"title\" : \"My key\",\r\n"
            + "      \"type\": \"invalidtype\"\r\n"
            + "    }\r\n"
            + "  }\r\n"
            + "}";
        List<Error> errors = schema.validate(input, InputFormat.JSON, executionContext -> {
            /*
             * By default since Draft 2019-09 the format keyword only generates annotations
             * and not assertions.
             */
            executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
        });        
        assertEquals(2, errors.size());
    }

    @Test
    void location() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://schema/myschema\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"startDate\": {\r\n"
                + "      \"format\": \"date\",\r\n"
                + "      \"minLength\": 6\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"startDate\": \"1\"\r\n"
                + "}";

        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(),
                builder -> builder.nodeReader(nodeReader -> nodeReader.locationAware()));

        Schema schema = schemaRegistry.getSchema(schemaData, InputFormat.JSON);
        List<Error> errors = schema.validate(inputData, InputFormat.JSON, executionContext -> {
            executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
        });
        Error format = errors.get(0);
        JsonLocation formatInstanceNodeTokenLocation = JsonNodes.tokenStreamLocationOf(format.getInstanceNode());
        JsonLocation formatSchemaNodeTokenLocation = JsonNodes.tokenStreamLocationOf(format.getSchemaNode());
        Error minLength = errors.get(1);
        JsonLocation minLengthInstanceNodeTokenLocation = JsonNodes.tokenStreamLocationOf(minLength.getInstanceNode());
        JsonLocation minLengthSchemaNodeTokenLocation = JsonNodes.tokenStreamLocationOf(minLength.getSchemaNode());

        assertEquals("format", format.getKeyword());
        assertEquals("date", format.getSchemaNode().asText());
        assertEquals(5, formatSchemaNodeTokenLocation.getLineNr());
        assertEquals(17, formatSchemaNodeTokenLocation.getColumnNr());
        assertEquals("1", format.getInstanceNode().asText());
        assertEquals(2, formatInstanceNodeTokenLocation.getLineNr());
        assertEquals(16, formatInstanceNodeTokenLocation.getColumnNr());
        assertEquals("minLength", minLength.getKeyword());
        assertEquals("6", minLength.getSchemaNode().asText());
        assertEquals(6, minLengthSchemaNodeTokenLocation.getLineNr());
        assertEquals(20, minLengthSchemaNodeTokenLocation.getColumnNr());
        assertEquals("1", minLength.getInstanceNode().asText());
        assertEquals(2, minLengthInstanceNodeTokenLocation.getLineNr());
        assertEquals(16, minLengthInstanceNodeTokenLocation.getColumnNr());
        assertEquals(16, minLengthInstanceNodeTokenLocation.getColumnNr());
    }

    @Test
    void annotation() {
        String inputData = "{ \"hello\": \"world\"}";
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft201909());
        Schema schema = schemaRegistry.getSchema(SchemaLocation.of("classpath:schema/example-ref.json"));

        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.HIERARCHICAL, executionContext -> {
            executionContext.executionConfig(executionConfig -> executionConfig
                    .annotationCollectionEnabled(true)
                    .annotationCollectionFilter(keyword -> true)
                    .formatAssertionsEnabled(true));
        });
        assertNotNull(outputUnit);
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
    
    @Test
    void defaults() throws IOException {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\r\n"
                + "  \"title\": \"Schema with default values \",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"intValue\": {\r\n"
                + "      \"type\": \"integer\",\r\n"
                + "      \"default\": 15, \r\n"
                + "      \"minimum\": 20\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"required\": [\"intValue\"]\r\n"
                + "}";
        
        String inputData = "{}";
        
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft4());
        Schema schema =  schemaRegistry.getSchema(schemaData);

        JsonNode inputNode = JsonMapperFactory.getInstance().readTree(inputData);
        Result result = schema.walk(inputNode, true, executionContext -> executionContext.walkConfig(
                walkConfig -> walkConfig.applyDefaultsStrategy(applyDefaultsStrategy -> applyDefaultsStrategy
                        .applyArrayDefaults(true).applyPropertyDefaults(true).applyPropertyDefaultsIfNull(true))));
        assertFalse(result.getErrors().isEmpty());
        assertEquals("{\"intValue\":15}", inputNode.toString());
    }
}
