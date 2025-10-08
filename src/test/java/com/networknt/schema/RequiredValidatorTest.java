/*
 * Copyright (c) 2025 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * RequiredValidatorTest.
 */
class RequiredValidatorTest {
    /**
     * Tests that when validating requests with required read only properties they
     * are ignored.
     */
    @Test
    void validateRequestRequiredReadOnlyShouldBeIgnored() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"amount\": {\r\n"
                + "      \"type\": \"number\",\r\n"
                + "      \"writeOnly\": true\r\n"
                + "    },\r\n"
                + "    \"description\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"name\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"readOnly\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"required\": [\r\n"
                + "    \"amount\",\r\n"
                + "    \"description\",\r\n"
                + "    \"name\"\r\n"
                + "  ]\r\n"
                + "}";
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "{\r\n"
                + "  \"foo\":\"hello\",\r\n"
                + "  \"bar\":\"world\"\r\n"
                + "}";
		List<Error> messages = new ArrayList<>(
				schema.validate(inputData, InputFormat.JSON, executionContext -> executionContext
						.executionConfig(executionConfig -> executionConfig.readOnly(true))));
        assertEquals(messages.size(), 2);
        Error message = messages.get(0);
        assertEquals("/required", message.getEvaluationPath().toString());
        assertEquals("amount", message.getProperty());
        message = messages.get(1);
        assertEquals("/required", message.getEvaluationPath().toString());
        assertEquals("description", message.getProperty());
    }

    /**
     * Tests that when validating responses with required write only properties they
     * are ignored.
     */
    @Test
    void validateResponseRequiredWriteOnlyShouldBeIgnored() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"amount\": {\r\n"
                + "      \"type\": \"number\",\r\n"
                + "      \"writeOnly\": true\r\n"
                + "    },\r\n"
                + "    \"description\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"name\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"readOnly\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"required\": [\r\n"
                + "    \"amount\",\r\n"
                + "    \"description\",\r\n"
                + "    \"name\"\r\n"
                + "  ]\r\n"
                + "}";
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "{\r\n"
                + "  \"foo\":\"hello\",\r\n"
                + "  \"bar\":\"world\"\r\n"
                + "}";
		List<Error> messages = new ArrayList<>(
				schema.validate(inputData, InputFormat.JSON, executionContext -> executionContext
						.executionConfig(executionConfig -> executionConfig.writeOnly(true))));
        assertEquals(messages.size(), 2);
        Error message = messages.get(0);
        assertEquals("/required", message.getEvaluationPath().toString());
        assertEquals("description", message.getProperty());
        message = messages.get(1);
        assertEquals("/required", message.getEvaluationPath().toString());
        assertEquals("name", message.getProperty());
    }

    /**
     * Tests that when validating requests with required read only properties they
     * are ignored.
     */
    @Test
    void validateRequestRequired() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"amount\": {\r\n"
                + "      \"type\": \"number\",\r\n"
                + "      \"writeOnly\": true\r\n"
                + "    },\r\n"
                + "    \"description\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"name\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"readOnly\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"required\": [\r\n"
                + "    \"amount\",\r\n"
                + "    \"description\",\r\n"
                + "    \"name\"\r\n"
                + "  ]\r\n"
                + "}";
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "{\r\n"
                + "  \"amount\":10,\r\n"
                + "  \"description\":\"world\"\r\n"
                + "}";
		List<Error> messages = new ArrayList<>(
				schema.validate(inputData, InputFormat.JSON, executionContext -> executionContext
						.executionConfig(executionConfig -> executionConfig.readOnly(true))));
        assertEquals(messages.size(), 0);
    }

    /**
     * Tests that when validating response with required write only properties they
     * are ignored.
     */
    @Test
    void validateResponseRequired() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"amount\": {\r\n"
                + "      \"type\": \"number\",\r\n"
                + "      \"writeOnly\": true\r\n"
                + "    },\r\n"
                + "    \"description\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"name\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"readOnly\": true\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"required\": [\r\n"
                + "    \"amount\",\r\n"
                + "    \"description\",\r\n"
                + "    \"name\"\r\n"
                + "  ]\r\n"
                + "}";
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "{\r\n"
                + "  \"description\":\"world\",\r\n"
                + "  \"name\":\"hello\"\r\n"
                + "}";
		List<Error> messages = new ArrayList<>(
				schema.validate(inputData, InputFormat.JSON, executionContext -> executionContext
						.executionConfig(executionConfig -> executionConfig.writeOnly(true))));
        assertEquals(messages.size(), 0);
    }
}
