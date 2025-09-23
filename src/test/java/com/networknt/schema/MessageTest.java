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

/**
 * Test for messages.
 */
class MessageTest {
    static class EqualsValidator extends BaseKeywordValidator {
        private final String value;

        EqualsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                Schema parentSchema, Keyword keyword,
                SchemaContext schemaContext) {
            super(keyword, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
            this.value = schemaNode.textValue();
        }

        @Override
        public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                JsonNodePath instanceLocation) {
            if (!node.asText().equals(value)) {
                executionContext.addError(error().message("must be equal to ''{0}''")
                                .arguments(value)
                                .instanceLocation(instanceLocation).instanceNode(node).build());
            }
        }
    }
    
    static class EqualsKeyword implements Keyword {
        
        @Override
        public String getValue() {
            return "equals";
        }

        @Override
        public KeywordValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath,
                JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext)
                throws JsonSchemaException, Exception {
            return new EqualsValidator(schemaLocation, evaluationPath, schemaNode, parentSchema, this, schemaContext);
        }
    }

    @Test
    void message() {
        Dialect dialect = Dialect.builder(Dialects.getDraft202012().getIri(), Dialects.getDraft202012())
                .keyword(new EqualsKeyword()).build();
        SchemaRegistry factory = SchemaRegistry.withDialect(dialect);
        String schemaData = "{\r\n"
                + "  \"type\": \"string\",\r\n"
                + "  \"equals\": \"helloworld\"\r\n"
                + "}";
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages = schema.validate("\"helloworlda\"", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals(": must be equal to 'helloworld'", messages.iterator().next().toString());
        
        messages = schema.validate("\"helloworld\"", InputFormat.JSON);
        assertEquals(0, messages.size());
    }
}
