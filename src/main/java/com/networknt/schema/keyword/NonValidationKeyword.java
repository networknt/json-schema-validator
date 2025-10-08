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
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Used for Keywords that have no validation aspect, but are part of the metaschema.
 */
public class NonValidationKeyword extends AbstractKeyword {

    private static final class Validator extends AbstractKeywordValidator {
        public Validator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
                Schema parentSchema, SchemaContext schemaContext, Keyword keyword) {
            super(keyword, schemaNode, schemaLocation, evaluationPath);
            String id = schemaContext.resolveSchemaId(schemaNode);
            String anchor = schemaContext.getDialect().readAnchor(schemaNode);
            String dynamicAnchor = schemaContext.getDialect().readDynamicAnchor(schemaNode);
            if (id != null || anchor != null || dynamicAnchor != null) {
                // Used to register schema resources with $id
                schemaContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
            }
            if ("$defs".equals(keyword.getValue()) || "definitions".equals(keyword.getValue())) {
                for (Iterator<Entry<String, JsonNode>> field = schemaNode.fields(); field.hasNext(); ) {
                    Entry<String, JsonNode> property = field.next();
                    SchemaLocation location = schemaLocation.append(property.getKey());
                    Schema schema = schemaContext.newSchema(location, evaluationPath.append(property.getKey()),
                            property.getValue(), parentSchema);
                    schemaContext.getSchemaReferences().put(location.toString(), schema);
                }
            }
        }

        @Override
        public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
            // Do nothing
        }
    }

    public NonValidationKeyword(String keyword) {
        super(keyword);
    }

    @Override
    public KeywordValidator newValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
                                      Schema parentSchema, SchemaContext schemaContext) {
        return new Validator(schemaLocation, evaluationPath, schemaNode, parentSchema, schemaContext, this);
    }
}
