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

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * Test for messages.
 */
class MessageTest {
    static class EqualsValidator extends BaseJsonValidator {
        private static final ErrorMessageType ERROR_MESSAGE_TYPE = () -> "equals";
        
        private final String value;

        EqualsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                JsonSchema parentSchema, Keyword keyword,
                ValidationContext validationContext, boolean suppressSubSchemaRetrieval) {
            super(schemaLocation, evaluationPath, schemaNode, parentSchema, ERROR_MESSAGE_TYPE, keyword, validationContext,
                    suppressSubSchemaRetrieval);
            this.value = schemaNode.textValue();
        }

        @Override
        public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                JsonNodePath instanceLocation) {
            if (!node.asText().equals(value)) {
                return Collections
                        .singleton(message().message("{0}: must be equal to ''{1}''")
                                .arguments(value)
                                .instanceLocation(instanceLocation).instanceNode(node).build());
            }
            return Collections.emptySet();
        }
    }
    
    static class EqualsKeyword implements Keyword {
        
        @Override
        public String getValue() {
            return "equals";
        }

        @Override
        public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath,
                JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext)
                throws JsonSchemaException, Exception {
            return new EqualsValidator(schemaLocation, evaluationPath, schemaNode, parentSchema, this, validationContext, false);
        }
    }

    @Test
    void message() {
        JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012().getIri(), JsonMetaSchema.getV202012())
                .keyword(new EqualsKeyword()).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema));
        String schemaData = "{\r\n"
                + "  \"type\": \"string\",\r\n"
                + "  \"equals\": \"helloworld\"\r\n"
                + "}";
        JsonSchema schema = factory.getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate("\"helloworlda\"", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("$: must be equal to 'helloworld'", messages.iterator().next().getMessage());
        
        messages = schema.validate("\"helloworld\"", InputFormat.JSON);
        assertEquals(0, messages.size());
    }
}
