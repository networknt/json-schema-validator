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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.format.PatternFormat;
import com.networknt.schema.output.OutputUnit;

/**
 * Test for format validator.
 */
class FormatValidatorTest {
    @Test
    void unknownFormatNoVocab() {
        String schemaData = "{\r\n"
                + "  \"format\":\"unknown\"\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate("\"hello\"", InputFormat.JSON, executionContext -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertEquals(0, messages.size());
    }

    @Test
    void unknownFormatNoVocabStrictTrue() {
        String schemaData = "{\r\n"
                + "  \"format\":\"unknown\"\r\n"
                + "}";
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().strict("format", true).build();
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData, config);
        Set<ValidationMessage> messages = schema.validate("\"hello\"", InputFormat.JSON, executionContext -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertEquals(1, messages.size());
        assertEquals("format.unknown", messages.iterator().next().getMessageKey());
    }

    @Test
    void unknownFormatAssertionsVocab() {
        String metaSchemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"$id\": \"https://www.example.com/format-assertion/schema\",\r\n"
                + "  \"$vocabulary\": {\r\n"
                + "    \"https://json-schema.org/draft/2020-12/vocab/format-assertion\": true,\r\n"
                + "    \"https://json-schema.org/draft/2020-12/vocab/applicator\": true,\r\n"
                + "    \"https://json-schema.org/draft/2020-12/vocab/core\": true\r\n"
                + "  },\r\n"
                + "  \"allOf\": [\r\n"
                + "    { \"$ref\": \"https://json-schema.org/draft/2020-12/meta/applicator\" },\r\n"
                + "    { \"$ref\": \"https://json-schema.org/draft/2020-12/meta/core\" }\r\n"
                + "  ]\r\n"
                + "}";

        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://www.example.com/format-assertion/schema\",\r\n"
                + "  \"format\":\"unknown\"\r\n"
                + "}";
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = JsonSchemaFactory
                .getInstance(VersionFlag.V202012,
                        builder -> builder
                                .schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections.singletonMap("https://www.example.com/format-assertion/schema", metaSchemaData))))
                .getSchema(schemaData, config);
        Set<ValidationMessage> messages = schema.validate("\"hello\"", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("format.unknown", messages.iterator().next().getMessageKey());
    }

    @Test
    void unknownFormatShouldCollectAnnotations() {
        String schemaData = "{\r\n"
                + "  \"format\":\"unknown\"\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData);
        OutputUnit outputUnit = schema.validate("\"hello\"", InputFormat.JSON, OutputFormat.HIERARCHICAL, executionContext -> {
            executionContext.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionContext.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
        });
        assertEquals("unknown", outputUnit.getAnnotations().get("format"));
        assertTrue(outputUnit.isValid()); // as no assertion vocab and assertions not enabled
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
    void formatAssertions(FormatInput formatInput) {
        String formatSchema = "{\r\n"
                + "  \"type\": \"string\",\r\n"
                + "  \"format\": \""+formatInput.format+"\"\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(formatSchema, config);
        Set<ValidationMessage> messages = schema.validate("\"inval!i:d^(abc]\"", InputFormat.JSON, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertFalse(messages.isEmpty());
    }

    /**
     * This tests that the changes to use message key doesn't cause a regression to
     * the existing message.
     */
    @SuppressWarnings("deprecation")
    @Test
    void patternFormatDeprecated() {
        JsonMetaSchema customMetaSchema = JsonMetaSchema
                .builder("https://www.example.com/schema", JsonMetaSchema.getV7())
                .formats(formats -> {
                    PatternFormat format = new PatternFormat("custom", "test", "must be test");
                    formats.put(format.getName(), format);
                })
                .build();

        JsonSchemaFactory factory = new JsonSchemaFactory.Builder().defaultMetaSchemaIri(customMetaSchema.getIri())
                .metaSchema(customMetaSchema).build();
        String formatSchema = "{\r\n"
                + "  \"type\": \"string\",\r\n"
                + "  \"format\": \"custom\"\r\n"
                + "}";
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(formatSchema, config);
        Set<ValidationMessage> messages = schema.validate("\"inval!i:d^(abc]\"", InputFormat.JSON, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertFalse(messages.isEmpty());
        assertEquals(": does not match the custom pattern must be test", messages.iterator().next().getMessage());
    }

    static class CustomNumberFormat implements Format {
        private final BigDecimal compare;
        
        CustomNumberFormat(BigDecimal compare) {
            this.compare = compare;
        }

        @Override
        public boolean matches(ExecutionContext executionContext, ValidationContext validationContext, JsonNode value) {
            JsonType nodeType = TypeFactory.getValueNodeType(value, validationContext.getConfig());
            if (nodeType != JsonType.NUMBER && nodeType != JsonType.INTEGER) {
                return true;
            }
            BigDecimal number = value.isBigDecimal() ? value.decimalValue() : BigDecimal.valueOf(value.doubleValue());
            number = new BigDecimal(number.toPlainString());
            return number.compareTo(compare) == 0;
        }

        @Override
        public String getName() {
            return "custom-number";
        }
    }

    @Test
    void shouldAllowNumberFormat() {
        JsonMetaSchema customMetaSchema = JsonMetaSchema
                .builder("https://www.example.com/schema", JsonMetaSchema.getV7())
                .formats(formats -> {
                    CustomNumberFormat format = new CustomNumberFormat(new BigDecimal("12345"));
                    formats.put(format.getName(), format);
                })
                .build();

        JsonSchemaFactory factory = new JsonSchemaFactory.Builder().defaultMetaSchemaIri(customMetaSchema.getIri())
                .metaSchema(customMetaSchema).build();
        String formatSchema = "{\r\n"
                + "  \"type\": \"number\",\r\n"
                + "  \"format\": \"custom-number\"\r\n"
                + "}";
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(formatSchema, config);
        Set<ValidationMessage> messages = schema.validate("123451", InputFormat.JSON, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertFalse(messages.isEmpty());
        assertEquals(": does not match the custom-number pattern ", messages.iterator().next().getMessage());
        messages = schema.validate("12345", InputFormat.JSON, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        assertTrue(messages.isEmpty());
        
    }

    @Test
    void draft7DisableFormat() {
        String schemaData = "{\r\n"
                + "  \"format\":\"uri\"\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V7).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate("\"hello\"", InputFormat.JSON, executionContext -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(false);
        });
        assertEquals(0, messages.size());
    }
}
