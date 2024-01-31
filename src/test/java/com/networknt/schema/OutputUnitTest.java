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

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.output.OutputUnit;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * OutputUnitTest.
 * 
 * @see <a href=
 *      "https://github.com/json-schema-org/json-schema-spec/blob/main/jsonschema-validation-output-machines.md">A
 *      Specification for Machine-Readable Output for JSON Schema Validation and
 *      Annotation</a>
 */
public class OutputUnitTest {
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
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        JsonSchema schema = factory.getSchema(schemaData, config);
        
        String inputData = inputData1;
        
        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.LIST, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionConfiguration.getExecutionConfig().setAnnotationCollectionPredicate(keyword -> true);
        });
        String output = JsonMapperFactory.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(outputUnit);
        String expected = "{\r\n"
                + "  \"valid\" : false,\r\n"
                + "  \"details\" : [ {\r\n"
                + "    \"valid\" : false,\r\n"
                + "    \"evaluationPath\" : \"/properties/foo/allOf/0\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#/properties/foo/allOf/0\",\r\n"
                + "    \"instanceLocation\" : \"/foo\",\r\n"
                + "    \"errors\" : {\r\n"
                + "      \"required\" : \"required property 'unspecified-prop' not found\"\r\n"
                + "    }\r\n"
                + "  }, {\r\n"
                + "    \"valid\" : false,\r\n"
                + "    \"evaluationPath\" : \"/properties/foo/allOf/1/properties/foo-prop\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#/properties/foo/allOf/1/properties/foo-prop\",\r\n"
                + "    \"instanceLocation\" : \"/foo/foo-prop\",\r\n"
                + "    \"errors\" : {\r\n"
                + "      \"const\" : \"must be a constant value 1\"\r\n"
                + "    },\r\n"
                + "    \"droppedAnnotations\" : {\r\n"
                + "      \"title\" : \"foo-prop-title\"\r\n"
                + "    }\r\n"
                + "  }, {\r\n"
                + "    \"valid\" : false,\r\n"
                + "    \"evaluationPath\" : \"/properties/bar/$ref/properties/bar-prop\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#/$defs/bar/properties/bar-prop\",\r\n"
                + "    \"instanceLocation\" : \"/bar/bar-prop\",\r\n"
                + "    \"errors\" : {\r\n"
                + "      \"minimum\" : \"must have a minimum value of 10\"\r\n"
                + "    },\r\n"
                + "    \"droppedAnnotations\" : {\r\n"
                + "      \"title\" : \"bar-prop-title\"\r\n"
                + "    }\r\n"
                + "  }, {\r\n"
                + "    \"valid\" : false,\r\n"
                + "    \"evaluationPath\" : \"/properties/foo/allOf/1\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#/properties/foo/allOf/1\",\r\n"
                + "    \"instanceLocation\" : \"/foo\",\r\n"
                + "    \"droppedAnnotations\" : {\r\n"
                + "      \"properties\" : [ \"foo-prop\" ],\r\n"
                + "      \"title\" : \"foo-title\",\r\n"
                + "      \"additionalProperties\" : [ \"foo-prop\", \"other-prop\" ]\r\n"
                + "    }\r\n"
                + "  }, {\r\n"
                + "    \"valid\" : false,\r\n"
                + "    \"evaluationPath\" : \"/properties/bar/$ref\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#/$defs/bar\",\r\n"
                + "    \"instanceLocation\" : \"/bar\",\r\n"
                + "    \"droppedAnnotations\" : {\r\n"
                + "      \"properties\" : [ \"bar-prop\" ],\r\n"
                + "      \"title\" : \"bar-title\"\r\n"
                + "    }\r\n"
                + "  }, {\r\n"
                + "    \"valid\" : false,\r\n"
                + "    \"evaluationPath\" : \"\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#\",\r\n"
                + "    \"instanceLocation\" : \"\",\r\n"
                + "    \"droppedAnnotations\" : {\r\n"
                + "      \"properties\" : [ \"foo\", \"bar\" ],\r\n"
                + "      \"title\" : \"root\"\r\n"
                + "    }\r\n"
                + "  } ]\r\n"
                + "}";
        assertEquals(expected, output);
    }

    @Test
    void annotationCollectionHierarchical() throws JsonProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        JsonSchema schema = factory.getSchema(schemaData, config);

        String inputData = inputData1;
        
        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.HIERARCHICAL, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionConfiguration.getExecutionConfig().setAnnotationCollectionPredicate(keyword -> true);
        });
        String output = JsonMapperFactory.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(outputUnit);
        String expected = "{\r\n"
                + "  \"valid\" : false,\r\n"
                + "  \"evaluationPath\" : \"\",\r\n"
                + "  \"schemaLocation\" : \"https://json-schema.org/schemas/example#\",\r\n"
                + "  \"instanceLocation\" : \"\",\r\n"
                + "  \"droppedAnnotations\" : {\r\n"
                + "    \"properties\" : [ \"foo\", \"bar\" ],\r\n"
                + "    \"title\" : \"root\"\r\n"
                + "  },\r\n"
                + "  \"details\" : [ {\r\n"
                + "    \"valid\" : false,\r\n"
                + "    \"evaluationPath\" : \"/properties/foo/allOf/0\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#/properties/foo/allOf/0\",\r\n"
                + "    \"instanceLocation\" : \"/foo\",\r\n"
                + "    \"errors\" : {\r\n"
                + "      \"required\" : \"required property 'unspecified-prop' not found\"\r\n"
                + "    }\r\n"
                + "  }, {\r\n"
                + "    \"valid\" : false,\r\n"
                + "    \"evaluationPath\" : \"/properties/foo/allOf/1\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#/properties/foo/allOf/1\",\r\n"
                + "    \"instanceLocation\" : \"/foo\",\r\n"
                + "    \"droppedAnnotations\" : {\r\n"
                + "      \"properties\" : [ \"foo-prop\" ],\r\n"
                + "      \"title\" : \"foo-title\",\r\n"
                + "      \"additionalProperties\" : [ \"foo-prop\", \"other-prop\" ]\r\n"
                + "    },\r\n"
                + "    \"details\" : [ {\r\n"
                + "      \"valid\" : false,\r\n"
                + "      \"evaluationPath\" : \"/properties/foo/allOf/1/properties/foo-prop\",\r\n"
                + "      \"schemaLocation\" : \"https://json-schema.org/schemas/example#/properties/foo/allOf/1/properties/foo-prop\",\r\n"
                + "      \"instanceLocation\" : \"/foo/foo-prop\",\r\n"
                + "      \"errors\" : {\r\n"
                + "        \"const\" : \"must be a constant value 1\"\r\n"
                + "      },\r\n"
                + "      \"droppedAnnotations\" : {\r\n"
                + "        \"title\" : \"foo-prop-title\"\r\n"
                + "      }\r\n"
                + "    } ]\r\n"
                + "  }, {\r\n"
                + "    \"valid\" : false,\r\n"
                + "    \"evaluationPath\" : \"/properties/bar/$ref\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#/$defs/bar\",\r\n"
                + "    \"instanceLocation\" : \"/bar\",\r\n"
                + "    \"droppedAnnotations\" : {\r\n"
                + "      \"properties\" : [ \"bar-prop\" ],\r\n"
                + "      \"title\" : \"bar-title\"\r\n"
                + "    },\r\n"
                + "    \"details\" : [ {\r\n"
                + "      \"valid\" : false,\r\n"
                + "      \"evaluationPath\" : \"/properties/bar/$ref/properties/bar-prop\",\r\n"
                + "      \"schemaLocation\" : \"https://json-schema.org/schemas/example#/$defs/bar/properties/bar-prop\",\r\n"
                + "      \"instanceLocation\" : \"/bar/bar-prop\",\r\n"
                + "      \"errors\" : {\r\n"
                + "        \"minimum\" : \"must have a minimum value of 10\"\r\n"
                + "      },\r\n"
                + "      \"droppedAnnotations\" : {\r\n"
                + "        \"title\" : \"bar-prop-title\"\r\n"
                + "      }\r\n"
                + "    } ]\r\n"
                + "  } ]\r\n"
                + "}";
        assertEquals(expected, output);
    }

    @Test
    void annotationCollectionHierarchical2() throws JsonProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        JsonSchema schema = factory.getSchema(schemaData, config);

        String inputData = inputData2;
        
        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.HIERARCHICAL, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionConfiguration.getExecutionConfig().setAnnotationCollectionPredicate(keyword -> true);
        });
        String output = JsonMapperFactory.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(outputUnit);
        String expected = "{\r\n"
                + "  \"valid\" : true,\r\n"
                + "  \"evaluationPath\" : \"\",\r\n"
                + "  \"schemaLocation\" : \"https://json-schema.org/schemas/example#\",\r\n"
                + "  \"instanceLocation\" : \"\",\r\n"
                + "  \"annotations\" : {\r\n"
                + "    \"properties\" : [ \"foo\", \"bar\" ],\r\n"
                + "    \"title\" : \"root\"\r\n"
                + "  },\r\n"
                + "  \"details\" : [ {\r\n"
                + "    \"valid\" : true,\r\n"
                + "    \"evaluationPath\" : \"/properties/foo/allOf/1\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#/properties/foo/allOf/1\",\r\n"
                + "    \"instanceLocation\" : \"/foo\",\r\n"
                + "    \"annotations\" : {\r\n"
                + "      \"properties\" : [ \"foo-prop\" ],\r\n"
                + "      \"title\" : \"foo-title\",\r\n"
                + "      \"additionalProperties\" : [ \"foo-prop\", \"unspecified-prop\" ]\r\n"
                + "    },\r\n"
                + "    \"details\" : [ {\r\n"
                + "      \"valid\" : true,\r\n"
                + "      \"evaluationPath\" : \"/properties/foo/allOf/1/properties/foo-prop\",\r\n"
                + "      \"schemaLocation\" : \"https://json-schema.org/schemas/example#/properties/foo/allOf/1/properties/foo-prop\",\r\n"
                + "      \"instanceLocation\" : \"/foo/foo-prop\",\r\n"
                + "      \"annotations\" : {\r\n"
                + "        \"title\" : \"foo-prop-title\"\r\n"
                + "      }\r\n"
                + "    } ]\r\n"
                + "  }, {\r\n"
                + "    \"valid\" : true,\r\n"
                + "    \"evaluationPath\" : \"/properties/bar/$ref\",\r\n"
                + "    \"schemaLocation\" : \"https://json-schema.org/schemas/example#/$defs/bar\",\r\n"
                + "    \"instanceLocation\" : \"/bar\",\r\n"
                + "    \"annotations\" : {\r\n"
                + "      \"properties\" : [ \"bar-prop\" ],\r\n"
                + "      \"title\" : \"bar-title\"\r\n"
                + "    },\r\n"
                + "    \"details\" : [ {\r\n"
                + "      \"valid\" : true,\r\n"
                + "      \"evaluationPath\" : \"/properties/bar/$ref/properties/bar-prop\",\r\n"
                + "      \"schemaLocation\" : \"https://json-schema.org/schemas/example#/$defs/bar/properties/bar-prop\",\r\n"
                + "      \"instanceLocation\" : \"/bar/bar-prop\",\r\n"
                + "      \"annotations\" : {\r\n"
                + "        \"title\" : \"bar-prop-title\"\r\n"
                + "      }\r\n"
                + "    } ]\r\n"
                + "  } ]\r\n"
                + "}";
        assertEquals(expected, output);
    }
}
