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

package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Used for Keywords that have no validation aspect, but are part of the metaschema.
 */
public class NonValidationKeyword extends AbstractKeyword {

    private static final class Validator extends AbstractKeywordValidator {
        public Validator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                JsonSchema parentSchema, ValidationContext validationContext, Keyword keyword) {
            super(keyword, schemaNode, schemaLocation, evaluationPath);
            String id = validationContext.resolveSchemaId(schemaNode);
            String anchor = validationContext.getMetaSchema().readAnchor(schemaNode);
            String dynamicAnchor = validationContext.getMetaSchema().readDynamicAnchor(schemaNode);
            if (id != null || anchor != null || dynamicAnchor != null) {
                // Used to register schema resources with $id
                validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
            }
            if ("$defs".equals(keyword.getValue()) || "definitions".equals(keyword.getValue())) {
                for (Iterator<Entry<String, JsonNode>> field = schemaNode.fields(); field.hasNext(); ) {
                    Entry<String, JsonNode> property = field.next();
                    SchemaLocation location = schemaLocation.append(property.getKey());
                    JsonSchema schema = validationContext.newSchema(location, evaluationPath.append(property.getKey()),
                            property.getValue(), parentSchema);
                    validationContext.getSchemaReferences().put(location.toString(), schema);
                }
            }
        }

        @Override
        public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
            // Do nothing
        }
    }

    public NonValidationKeyword(String keyword) {
        super(keyword);
    }

    @Override
    public KeywordValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                                      JsonSchema parentSchema, ValidationContext validationContext) {
        return new Validator(schemaLocation, evaluationPath, schemaNode, parentSchema, validationContext, this);
    }
}
