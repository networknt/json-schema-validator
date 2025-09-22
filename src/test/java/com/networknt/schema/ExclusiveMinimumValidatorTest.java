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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.networknt.schema.Specification.Version;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.DisallowUnknownKeywordFactory;

/**
 * Test ExclusiveMinimumValidator validator.
 */
class ExclusiveMinimumValidatorTest {
    @Test
    void draftV4ShouldHaveExclusiveMinimum() {
        String schemaData = "{" +
                "  \"type\": \"object\"," +
                "  \"properties\": {" +
                "    \"value1\": {" +
                "      \"type\": \"number\"," +
                "      \"minimum\": 0," +
                "      \"exclusiveMinimum\": true" +
                "    }" +
                "  }" +
                "}";        
        Dialect dialect = Dialect.builder(Dialects.getDraft4())
                .unknownKeywordFactory(DisallowUnknownKeywordFactory.getInstance()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(Version.DRAFT_4,
                builder -> builder.metaSchema(dialect));
        JsonSchema schema = factory.getSchema(schemaData);
        String inputData = "{\"value1\":0}";
        String validData = "{\"value1\":0.1}";
        
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals(1, messages.stream().filter(m -> "minimum".equals(m.getKeyword())).count());
        
        messages = schema.validate(validData, InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void draftV6ShouldNotAllowExclusiveMinimumBoolean() {
        String schemaData = "{" +
                "  \"type\": \"object\"," +
                "  \"properties\": {" +
                "    \"value1\": {" +
                "      \"type\": \"number\"," +
                "      \"minimum\": 0," +
                "      \"exclusiveMinimum\": true" +
                "    }" +
                "  }" +
                "}";
        Dialect dialect = Dialect.builder(Dialects.getDraft6())
                .unknownKeywordFactory(DisallowUnknownKeywordFactory.getInstance()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(Version.DRAFT_6,
                builder -> builder.metaSchema(dialect));
        assertThrows(JsonSchemaException.class, () -> factory.getSchema(schemaData));
    }

    @Test
    void draftV7ShouldNotAllowExclusiveMinimumBoolean() {
        String schemaData = "{" +
                "  \"type\": \"object\"," +
                "  \"properties\": {" +
                "    \"value1\": {" +
                "      \"type\": \"number\"," +
                "      \"minimum\": 0," +
                "      \"exclusiveMinimum\": true" +
                "    }" +
                "  }" +
                "}";
        Dialect dialect = Dialect.builder(Dialects.getDraft7())
                .unknownKeywordFactory(DisallowUnknownKeywordFactory.getInstance()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(Version.DRAFT_7,
                builder -> builder.metaSchema(dialect));
        assertThrows(JsonSchemaException.class, () -> factory.getSchema(schemaData));
    }
}
