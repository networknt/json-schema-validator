/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.Set;

/**
 * Used for Keywords that have no validation aspect, but are part of the metaschema, where annotations may need to be collected.
 */
public class AnnotationKeyword extends AbstractKeyword {

    private static final class Validator extends AbstractJsonValidator {
        public Validator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                JsonSchema parentSchema, ValidationContext validationContext, Keyword keyword) {
            super(schemaLocation, evaluationPath, keyword, schemaNode);
        }

        @Override
        public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
            if (collectAnnotations(executionContext)) {
                Object value = getAnnotationValue(getSchemaNode());
                if (value != null) {
                    putAnnotation(executionContext,
                            annotation -> annotation.instanceLocation(instanceLocation).value(value));
                }
            }
            return Collections.emptySet();
        }

        private Object getAnnotationValue(JsonNode schemaNode) {
            if (schemaNode.isTextual()) {
                return schemaNode.textValue(); 
            } else if (schemaNode.isNumber()) {
                return schemaNode.numberValue();
            } else if (schemaNode.isObject()) {
                return schemaNode;
            }
            return null;
        }
    }

    public AnnotationKeyword(String keyword) {
        super(keyword);
    }

    @Override
    public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                                      JsonSchema parentSchema, ValidationContext validationContext) {
        return new Validator(schemaLocation, evaluationPath, schemaNode, parentSchema, validationContext, this);
    }
}
