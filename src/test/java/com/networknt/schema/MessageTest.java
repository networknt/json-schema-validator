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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.BaseKeywordValidator;
import com.networknt.schema.keyword.Keyword;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.path.NodePath;

/**
 * Test for messages.
 */
class MessageTest {
    static class EqualsValidator extends BaseKeywordValidator {
        private final String value;

        EqualsValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
                Schema parentSchema, Keyword keyword,
                SchemaContext schemaContext) {
            super(keyword, schemaNode, schemaLocation, parentSchema, schemaContext);
            this.value = schemaNode.textValue();
        }

        @Override
        public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                NodePath instanceLocation) {
            if (!node.asText().equals(value)) {
                executionContext.addError(error().message("must be equal to ''{0}''")
                                .arguments(value)
                                .instanceLocation(instanceLocation).instanceNode(node).evaluationPath(executionContext.getEvaluationPath()).build());
            }
        }
    }
    
    static class EqualsKeyword implements Keyword {
        
        @Override
        public String getValue() {
            return "equals";
        }

        @Override
        public KeywordValidator newValidator(SchemaLocation schemaLocation,
                JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext)
                throws SchemaException, Exception {
            return new EqualsValidator(schemaLocation, schemaNode, parentSchema, this, schemaContext);
        }
    }

    @Test
    void message() {
        Dialect dialect = Dialect.builder(Dialects.getDraft202012())
                .keyword(new EqualsKeyword())
                .build();
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(dialect);
        String schemaData = "{\r\n"
                + "  \"type\": \"string\",\r\n"
                + "  \"equals\": \"helloworld\"\r\n"
                + "}";
        Schema schema = schemaRegistry.getSchema(schemaData);
        List<Error> messages = schema.validate("\"helloworlda\"", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals(": must be equal to 'helloworld'", messages.iterator().next().toString());
        
        messages = schema.validate("\"helloworld\"", InputFormat.JSON);
        assertEquals(0, messages.size());
    }
}
