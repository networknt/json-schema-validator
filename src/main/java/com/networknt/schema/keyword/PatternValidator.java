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
import com.networknt.schema.FailFastAssertionException;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.regex.RegularExpression;
import com.networknt.schema.utils.JsonType;
import com.networknt.schema.utils.TypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PatternValidator extends BaseKeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(PatternValidator.class);
    private final String pattern;
    private final RegularExpression compiledPattern;

    public PatternValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.PATTERN, schemaNode, schemaLocation, parentSchema, schemaContext);

        this.pattern = Optional.ofNullable(schemaNode).filter(JsonNode::isTextual).map(JsonNode::textValue).orElse(null);
        try {
            this.compiledPattern = RegularExpression.compile(this.pattern, schemaContext);
        } catch (RuntimeException e) {
            e.setStackTrace(new StackTraceElement[0]);
            logger.error("Failed to compile pattern '{}': {}", this.pattern, e.getMessage());
            throw e;
        }
    }

    private boolean matches(String value) {
        return this.compiledPattern.matches(value);
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        JsonType nodeType = TypeFactory.getValueNodeType(node, this.schemaContext.getSchemaRegistryConfig());
        if (nodeType != JsonType.STRING) {
            return;
        }

        try {
            if (!matches(node.asText())) {
                executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                        .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(this.pattern).build());
                return;
            }
        } catch (SchemaException | FailFastAssertionException e) {
            throw e;
        } catch (RuntimeException e) {
            logger.error("Failed to apply pattern '{}' at {}: {}", this.pattern, instanceLocation, e.getMessage());
            throw e;
        }
    }
}
