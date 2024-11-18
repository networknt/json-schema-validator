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
import com.networknt.schema.regex.RegularExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class PatternValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(PatternValidator.class);
    private final String pattern;
    private final RegularExpression compiledPattern;

    public PatternValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.PATTERN, validationContext);

        this.pattern = Optional.ofNullable(schemaNode).filter(JsonNode::isTextual).map(JsonNode::textValue).orElse(null);
        try {
            this.compiledPattern = RegularExpression.compile(this.pattern, validationContext);
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
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return Collections.emptySet();
        }

        try {
            if (!matches(node.asText())) {
                return Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .failFast(executionContext.isFailFast()).arguments(this.pattern).build());
            }
        } catch (JsonSchemaException | FailFastAssertionException e) {
            throw e;
        } catch (RuntimeException e) {
            logger.error("Failed to apply pattern '{}' at {}: {}", this.pattern, instanceLocation, e.getMessage());
            throw e;
        }

        return Collections.emptySet();
    }
}
