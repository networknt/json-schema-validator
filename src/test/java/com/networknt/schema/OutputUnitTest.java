/*
 * Copyright (c) 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.output.OutputUnit;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * OutputUnitTest.
 * 
 * @see <a href=
 *      "https://github.com/json-schema-org/json-schema-spec/blob/main/output/jsonschema-validation-output-machines.md">A
 *      Specification for Machine-Readable Output for JSON Schema Validation and
 *      Annotation</a>
 */
class OutputUnitTest {
    String schemaData = "{\r\n"
            + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
            + "  \"$id\": \"https://json-schema.org/schemas/example\",\r\n"
            + "  \"type\": \"object\",\r\n"
            + "  \"title\": \"root\",\r\n"
            + "  \"properties\": {\r\n"
            + "    \"foo\": {\r\n"
            + "      \"allOf\": [\r\n"
            + "        { \"required\": [\"unspecified-prop\"] },\r\n"
            + "        {\r\n"
            + "          \"type\": \"object\",\r\n"
            + "          \"title\": \"foo-title\",\r\n"
            + "          \"properties\": {\r\n"
            + "            \"foo-prop\": {\r\n"
            + "              \"const\": 1,\r\n"
            + "              \"title\": \"foo-prop-title\"\r\n"
            + "            }\r\n"
            + "          },\r\n"
            + "          \"additionalProperties\": { \"type\": \"boolean\" }\r\n"
            + "        }\r\n"
            + "      ]\r\n"
            + "    },\r\n"
            + "    \"bar\": { \"$ref\": \"#/$defs/bar\" }\r\n"
            + "  },\r\n"
            + "  \"$defs\": {\r\n"
            + "    \"bar\": {\r\n"
            + "      \"type\": \"object\",\r\n"
            + "      \"title\": \"bar-title\",\r\n"
            + "      \"properties\": {\r\n"
            + "        \"bar-prop\": {\r\n"
            + "          \"type\": \"integer\",\r\n"
            + "          \"minimum\": 10,\r\n"
            + "          \"title\": \"bar-prop-title\"\r\n"
            + "        }\r\n"
            + "      }\r\n"
            + "    }\r\n"
            + "  }\r\n"
            + "}";
    
    String inputData1 = "{\r\n"
            + "  \"foo\": { \"foo-prop\": \"not 1\", \"other-prop\": false },\r\n"
            + "  \"bar\": { \"bar-prop\": 2 }\r\n"
            + "}";
    
    String inputData2 = "{\r\n"
            + "  \"foo\": {\r\n"
            + "    \"foo-prop\": 1,\r\n"
            + "    \"unspecified-prop\": true\r\n"
            + "  },\r\n"
            + "  \"bar\": { \"bar-prop\": 20 }\r\n"
            + "}";
    @Test
    void annotationCollectionList() throws JsonProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        
        String inputData = inputData1;
        
        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.LIST, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionConfiguration.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
        });
        String output = JsonMapperFactory.getInstance().writeValueAsString(outputUnit);
        String expected = "{\"valid\":false,\"details\":[{\"valid\":false,\"evaluationPath\":\"/properties/foo/allOf/0\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/properties/foo/allOf/0\",\"instanceLocation\":\"/foo\",\"errors\":{\"required\":\"required property 'unspecified-prop' not found\"}},{\"valid\":false,\"evaluationPath\":\"/properties/foo/allOf/1/properties/foo-prop\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/properties/foo/allOf/1/properties/foo-prop\",\"instanceLocation\":\"/foo/foo-prop\",\"errors\":{\"const\":\"must be the constant value '1'\"},\"droppedAnnotations\":{\"title\":\"foo-prop-title\"}},{\"valid\":false,\"evaluationPath\":\"/properties/bar/$ref/properties/bar-prop\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/$defs/bar/properties/bar-prop\",\"instanceLocation\":\"/bar/bar-prop\",\"errors\":{\"minimum\":\"must have a minimum value of 10\"},\"droppedAnnotations\":{\"title\":\"bar-prop-title\"}},{\"valid\":false,\"evaluationPath\":\"/properties/foo/allOf/1\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/properties/foo/allOf/1\",\"instanceLocation\":\"/foo\",\"droppedAnnotations\":{\"properties\":[\"foo-prop\"],\"title\":\"foo-title\",\"additionalProperties\":[\"foo-prop\",\"other-prop\"]}},{\"valid\":false,\"evaluationPath\":\"/properties/bar/$ref\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/$defs/bar\",\"instanceLocation\":\"/bar\",\"droppedAnnotations\":{\"properties\":[\"bar-prop\"],\"title\":\"bar-title\"}},{\"valid\":false,\"evaluationPath\":\"\",\"schemaLocation\":\"https://json-schema.org/schemas/example#\",\"instanceLocation\":\"\",\"droppedAnnotations\":{\"properties\":[\"foo\",\"bar\"],\"title\":\"root\"}}]}";
        assertEquals(expected, output);
    }

    @Test
    void annotationCollectionHierarchical() throws JsonProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);

        String inputData = inputData1;
        
        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.HIERARCHICAL, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionConfiguration.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
        });
        String output = JsonMapperFactory.getInstance().writeValueAsString(outputUnit);
        String expected = "{\"valid\":false,\"evaluationPath\":\"\",\"schemaLocation\":\"https://json-schema.org/schemas/example#\",\"instanceLocation\":\"\",\"droppedAnnotations\":{\"properties\":[\"foo\",\"bar\"],\"title\":\"root\"},\"details\":[{\"valid\":false,\"evaluationPath\":\"/properties/foo/allOf/0\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/properties/foo/allOf/0\",\"instanceLocation\":\"/foo\",\"errors\":{\"required\":\"required property 'unspecified-prop' not found\"}},{\"valid\":false,\"evaluationPath\":\"/properties/foo/allOf/1\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/properties/foo/allOf/1\",\"instanceLocation\":\"/foo\",\"droppedAnnotations\":{\"properties\":[\"foo-prop\"],\"title\":\"foo-title\",\"additionalProperties\":[\"foo-prop\",\"other-prop\"]},\"details\":[{\"valid\":false,\"evaluationPath\":\"/properties/foo/allOf/1/properties/foo-prop\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/properties/foo/allOf/1/properties/foo-prop\",\"instanceLocation\":\"/foo/foo-prop\",\"errors\":{\"const\":\"must be the constant value '1'\"},\"droppedAnnotations\":{\"title\":\"foo-prop-title\"}}]},{\"valid\":false,\"evaluationPath\":\"/properties/bar/$ref\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/$defs/bar\",\"instanceLocation\":\"/bar\",\"droppedAnnotations\":{\"properties\":[\"bar-prop\"],\"title\":\"bar-title\"},\"details\":[{\"valid\":false,\"evaluationPath\":\"/properties/bar/$ref/properties/bar-prop\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/$defs/bar/properties/bar-prop\",\"instanceLocation\":\"/bar/bar-prop\",\"errors\":{\"minimum\":\"must have a minimum value of 10\"},\"droppedAnnotations\":{\"title\":\"bar-prop-title\"}}]}]}";
        assertEquals(expected, output);
    }

    @Test
    void annotationCollectionHierarchical2() throws JsonProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);

        String inputData = inputData2;
        
        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.HIERARCHICAL, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionConfiguration.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
        });
        String output = JsonMapperFactory.getInstance().writeValueAsString(outputUnit);
        String expected = "{\"valid\":true,\"evaluationPath\":\"\",\"schemaLocation\":\"https://json-schema.org/schemas/example#\",\"instanceLocation\":\"\",\"annotations\":{\"properties\":[\"foo\",\"bar\"],\"title\":\"root\"},\"details\":[{\"valid\":true,\"evaluationPath\":\"/properties/foo/allOf/1\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/properties/foo/allOf/1\",\"instanceLocation\":\"/foo\",\"annotations\":{\"properties\":[\"foo-prop\"],\"title\":\"foo-title\",\"additionalProperties\":[\"foo-prop\",\"unspecified-prop\"]},\"details\":[{\"valid\":true,\"evaluationPath\":\"/properties/foo/allOf/1/properties/foo-prop\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/properties/foo/allOf/1/properties/foo-prop\",\"instanceLocation\":\"/foo/foo-prop\",\"annotations\":{\"title\":\"foo-prop-title\"}}]},{\"valid\":true,\"evaluationPath\":\"/properties/bar/$ref\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/$defs/bar\",\"instanceLocation\":\"/bar\",\"annotations\":{\"properties\":[\"bar-prop\"],\"title\":\"bar-title\"},\"details\":[{\"valid\":true,\"evaluationPath\":\"/properties/bar/$ref/properties/bar-prop\",\"schemaLocation\":\"https://json-schema.org/schemas/example#/$defs/bar/properties/bar-prop\",\"instanceLocation\":\"/bar/bar-prop\",\"annotations\":{\"title\":\"bar-prop-title\"}}]}]}";
        assertEquals(expected, output);
    }

    enum FormatInput {
        DATE_TIME("date-time"),
        DATE("date"),
        TIME("time"),
        DURATION("duration"),
        EMAIL("email"),
        IDN_EMAIL("idn-email"),
        HOSTNAME("hostname"),
        IDN_HOSTNAME("idn-hostname"),
        IPV4("ipv4"),
        IPV6("ipv6"),
        URI("uri"),
        URI_REFERENCE("uri-reference"),
        IRI("iri"),
        IRI_REFERENCE("iri-reference"),
        UUID("uuid"),
        JSON_POINTER("json-pointer"),
        RELATIVE_JSON_POINTER("relative-json-pointer"),
        REGEX("regex");

        String format;

        FormatInput(String format) {
            this.format = format;
        }
    }

    @ParameterizedTest
    @EnumSource(FormatInput.class)
    void formatAnnotation(FormatInput formatInput) {
        String formatSchema = "{\r\n"
                + "  \"type\": \"string\",\r\n"
                + "  \"format\": \""+formatInput.format+"\"\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(formatSchema, config);
        OutputUnit outputUnit = schema.validate("\"inval!i:d^(abc]\"", InputFormat.JSON, OutputFormat.LIST, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionConfiguration.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
        });
        assertTrue(outputUnit.isValid());
        OutputUnit details = outputUnit.getDetails().get(0);
        assertEquals(formatInput.format, details.getAnnotations().get("format"));
    }

    @ParameterizedTest
    @EnumSource(FormatInput.class)
    void formatAssertion(FormatInput formatInput) {
        String formatSchema = "{\r\n"
                + "  \"type\": \"string\",\r\n"
                + "  \"format\": \""+formatInput.format+"\"\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(formatSchema, config);
        OutputUnit outputUnit = schema.validate("\"inval!i:d^(abc]\"", InputFormat.JSON, OutputFormat.LIST, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionConfiguration.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
            executionConfiguration.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertFalse(outputUnit.isValid());
        OutputUnit details = outputUnit.getDetails().get(0);
        assertEquals(formatInput.format, details.getDroppedAnnotations().get("format"));
        assertNotNull(details.getErrors().get("format"));
    }

    @Test
    void typeUnion() {
        String typeSchema = "{\r\n"
                + "  \"type\": [\"string\",\"array\"]\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(typeSchema, config);
        OutputUnit outputUnit = schema.validate("1", InputFormat.JSON, OutputFormat.LIST, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionConfiguration.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
        });
        assertFalse(outputUnit.isValid());
        OutputUnit details = outputUnit.getDetails().get(0);
        assertNotNull(details.getErrors().get("type"));
    }
    
    @Test
    void unevaluatedProperties() throws JsonProcessingException {
        Map<String, String> external = new HashMap<>();

        String externalSchemaData = "{\r\n"
                + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                + "  \"$id\": \"https://www.example.org/point.json\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"required\": [\r\n"
                + "    \"type\",\r\n"
                + "    \"coordinates\"\r\n"
                + "  ],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"type\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"enum\": [\r\n"
                + "        \"Point\"\r\n"
                + "      ]\r\n"
                + "    },\r\n"
                + "    \"coordinates\": {\r\n"
                + "      \"type\": \"array\",\r\n"
                + "      \"minItems\": 2,\r\n"
                + "      \"items\": {\r\n"
                + "        \"type\": \"number\"\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        external.put("https://www.example.org/point.json", externalSchemaData);

        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"$ref\": \"https://www.example.org/point.json\",\r\n"
                + "  \"unevaluatedProperties\": false\r\n"
                + "}";

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(external)));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        
     // The following checks if the heirarchical output format is correct with multiple unevaluated properties
        String inputData = "{\r\n"
                + "  \"type\": \"Point\",\r\n"
                + "  \"hello\": \"Point\",\r\n"
                + "  \"world\": \"Point\",\r\n"
                + "  \"coordinates\": [1, 1]\r\n"
                + "}";
        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.HIERARCHICAL,
                executionContext -> executionContext.getExecutionConfig()
                        .setAnnotationCollectionFilter(keyword -> true));
        String output = JsonMapperFactory.getInstance().writeValueAsString(outputUnit);
        String expected = "{\"valid\":false,\"evaluationPath\":\"\",\"schemaLocation\":\"#\",\"instanceLocation\":\"\",\"errors\":{\"unevaluatedProperties\":[\"property 'hello' is not evaluated and the schema does not allow unevaluated properties\",\"property 'world' is not evaluated and the schema does not allow unevaluated properties\"]},\"droppedAnnotations\":{\"unevaluatedProperties\":[\"hello\",\"world\"]},\"details\":[{\"valid\":false,\"evaluationPath\":\"/$ref\",\"schemaLocation\":\"https://www.example.org/point.json#\",\"instanceLocation\":\"\",\"droppedAnnotations\":{\"properties\":[\"type\",\"coordinates\"]}}]}";
        assertEquals(expected, output);
    }

    /**
     * Test that anyOf doesn't short circuit if annotations are turned on.
     * 
     * @see <a href=
     *      "https://github.com/json-schema-org/json-schema-spec/blob/f8967bcbc6cee27753046f63024b55336a9b1b54/jsonschema-core.md?plain=1#L1717-L1720">anyOf</a>
     * @throws JsonProcessingException the exception
     */
    @Test
    void anyOf() throws JsonProcessingException {
        // Test that any of doesn't short circuit if annotations need to be collected
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"anyOf\": [\r\n"
                + "    {\r\n"
                + "      \"properties\": {\r\n"
                + "        \"foo\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    },\r\n"
                + "    {\r\n"
                + "      \"properties\": {\r\n"
                + "        \"bar\": {\r\n"
                + "          \"type\": \"integer\"\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  ]\r\n"
                + "}";
        
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        
        String inputData = "{\r\n"
                + "    \"foo\": \"hello\",\r\n"
                + "    \"bar\": 1\r\n"
                + "}";
        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.HIERARCHICAL, executionContext -> {
            executionContext.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionContext.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
        });
        String output = JsonMapperFactory.getInstance().writeValueAsString(outputUnit);
        String expected = "{\"valid\":true,\"evaluationPath\":\"\",\"schemaLocation\":\"#\",\"instanceLocation\":\"\",\"details\":[{\"valid\":true,\"evaluationPath\":\"/anyOf/0\",\"schemaLocation\":\"#/anyOf/0\",\"instanceLocation\":\"\",\"annotations\":{\"properties\":[\"foo\"]}},{\"valid\":true,\"evaluationPath\":\"/anyOf/1\",\"schemaLocation\":\"#/anyOf/1\",\"instanceLocation\":\"\",\"annotations\":{\"properties\":[\"bar\"]}}]}";
        assertEquals(expected, output);
    }

    @Test
    void listAssertionMapper() {
        String formatSchema = "{\r\n"
                + "  \"type\": \"string\"\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(formatSchema, config);
        OutputUnit outputUnit = schema.validate("1234", InputFormat.JSON, new OutputFormat.List(a -> a));
        assertFalse(outputUnit.isValid());
        OutputUnit details = outputUnit.getDetails().get(0);
        Object assertion = details.getErrors().get("type");
        assertInstanceOf(ValidationMessage.class, assertion);
    }

    @Test
    void hierarchicalAssertionMapper() {
        String formatSchema = "{\r\n"
                + "  \"type\": \"string\"\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(formatSchema, config);
        OutputUnit outputUnit = schema.validate("1234", InputFormat.JSON, new OutputFormat.Hierarchical(a -> a));
        assertFalse(outputUnit.isValid());
        Object assertion = outputUnit.getErrors().get("type");
        assertInstanceOf(ValidationMessage.class, assertion);
    }
}
