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
package com.networknt.schema.regex;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.OutputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.dialect.Dialects;

/**
 * RegularExpressionTest.
 */
public class RegularExpressionTest {
    private static final String DRAFT4_REGEX_FORMAT_SCHEMA = "{\r\n"
            + "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\r\n"
            + "  \"format\": \"regex\"\r\n"
            + "}";

    private static final String DRAFT7_REGEX_FORMAT_SCHEMA = "{\r\n"
            + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
            + "  \"format\": \"regex\"\r\n"
            + "}";

    private static final String LEGACY_IDENTITY_ESCAPE_REGEX = "\"\\\\a\"";
    private static final String ISSUE_1248_REGEX = "\"\\\\d+:\\\\[1-9]+|N\"";

    @Test
    public void testInvalidRegexValidatorECMA262() throws Exception {
        SchemaRegistryConfig schemaRegistryConfig = SchemaRegistryConfig.builder()
                .regularExpressionFactory(GraalJSRegularExpressionFactory.getInstance()).build();
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(),
                builder -> builder.schemaRegistryConfig(schemaRegistryConfig));
        Schema schema = schemaRegistry.getSchema("{\r\n"
                + "  \"format\": \"regex\"\r\n"
                + "}");
        List<Error> errors = schema.validate(LEGACY_IDENTITY_ESCAPE_REGEX, InputFormat.JSON, executionContext -> {
            executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
        });
        assertFalse(errors.isEmpty());
    }

    @Test
    public void testDraft4RegexFormatValidatorUsesNonUnicodeECMA262() {
        assertRegexFormatValid(GraalJSRegularExpressionFactory.getInstance(), DRAFT4_REGEX_FORMAT_SCHEMA,
                LEGACY_IDENTITY_ESCAPE_REGEX);
        assertRegexFormatValid(GraalJSRegularExpressionFactory.getInstance(), DRAFT4_REGEX_FORMAT_SCHEMA,
                ISSUE_1248_REGEX);
    }

    @Test
    public void testDraft7RegexFormatValidatorUsesNonUnicodeECMA262() {
        assertRegexFormatValid(GraalJSRegularExpressionFactory.getInstance(), DRAFT7_REGEX_FORMAT_SCHEMA,
                ISSUE_1248_REGEX);
    }

    @Test
    public void testDelegatingRegularExpressionFactoriesPreserveSchemaContext() {
        assertRegexFormatValid(ECMAScriptRegularExpressionFactory.getInstance(), DRAFT4_REGEX_FORMAT_SCHEMA,
                ISSUE_1248_REGEX);
        assertRegexFormatValid(new AllowRegularExpressionFactory(GraalJSRegularExpressionFactory.getInstance(),
                regex -> true), DRAFT4_REGEX_FORMAT_SCHEMA, ISSUE_1248_REGEX);
    }

    private static void assertRegexFormatValid(RegularExpressionFactory regularExpressionFactory, String schemaText,
            String valueText) {
        SchemaRegistryConfig schemaRegistryConfig = SchemaRegistryConfig.builder()
                .regularExpressionFactory(regularExpressionFactory).build();
        SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(Dialects.getDraft202012(),
                builder -> builder.schemaRegistryConfig(schemaRegistryConfig));
        Schema schema = schemaRegistry.getSchema(schemaText);
        boolean valid = schema.validate(valueText, InputFormat.JSON, OutputFormat.BOOLEAN,
                executionContext -> executionContext.executionConfig(
                        executionConfig -> executionConfig.formatAssertionsEnabled(true)));
        assertTrue(valid);
    }
}
