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

package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.utils.JsonType;
import com.networknt.schema.utils.TypeFactory;
import com.networknt.schema.SchemaContext;

import java.util.Base64;

/**
 * {@link KeywordValidator} for contentEncoding.
 * <p>
 * Note that since 2019-09 this keyword only generates annotations and not
 * errors.
 */
public class ContentEncodingValidator extends BaseKeywordValidator {
    private final String contentEncoding;

    /**
     * Constructor.
     * 
     * @param schemaLocation    the schema location
     * @param evaluationPath    the evaluation path
     * @param schemaNode        the schema node
     * @param parentSchema      the parent schema
     * @param schemaContext the schema context
     */
    public ContentEncodingValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.CONTENT_ENCODING, schemaNode, schemaLocation, parentSchema, schemaContext,
                evaluationPath);
        this.contentEncoding = schemaNode.textValue();
    }

    private boolean matches(String value) {
        if ("base64".equals(this.contentEncoding)) {
            try {
                Base64.getDecoder().decode(value);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        // Ignore non-strings
        JsonType nodeType = TypeFactory.getValueNodeType(node, this.schemaContext.getSchemaRegistryConfig());
        if (nodeType != JsonType.STRING) {
            return;
        }
        
        if (collectAnnotations(executionContext)) {
            putAnnotation(executionContext,
                    annotation -> annotation.instanceLocation(instanceLocation).value(this.contentEncoding));
        }

        if (!matches(node.asText())) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .arguments(this.contentEncoding)
                    .build());
        }
    }
}
