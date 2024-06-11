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

import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Collections;
import java.util.Set;
/**
 * {@link JsonValidator} for contentEncoding.
 * <p>
 * Note that since 2019-09 this keyword only generates annotations and not
 * assertions.
 */
public class ContentEncodingValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ContentEncodingValidator.class);
    private final String contentEncoding;

    /**
     * Constructor.
     * 
     * @param schemaLocation    the schema location
     * @param evaluationPath    the evaluation path
     * @param schemaNode        the schema node
     * @param parentSchema      the parent schema
     * @param validationContext the validation context
     */
    public ContentEncodingValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.CONTENT_ENCODING,
                validationContext);
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
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        // Ignore non-strings
        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return Collections.emptySet();
        }
        
        if (collectAnnotations(executionContext)) {
            putAnnotation(executionContext,
                    annotation -> annotation.instanceLocation(instanceLocation).value(this.contentEncoding));
        }

        if (!matches(node.asText())) {
            return Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .failFast(executionContext.isFailFast()).arguments(this.contentEncoding)
                    .build());
        }
        return Collections.emptySet();
    }
}
