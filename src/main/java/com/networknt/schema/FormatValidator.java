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
import com.networknt.schema.format.BaseFormatJsonValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

public class FormatValidator extends BaseFormatJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(FormatValidator.class);

    private final Format format;

    public FormatValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext, Format format, ValidatorTypeCode type) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, type, validationContext);
        this.format = format;
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);

        if (format != null) {
            if (collectAnnotations(executionContext)) {
                putAnnotation(executionContext,
                        annotation -> annotation.instanceLocation(instanceLocation).value(this.format.getName()));
            }
        }

        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return Collections.emptySet();
        }

        boolean assertionsEnabled = isAssertionsEnabled(executionContext);
        Set<ValidationMessage> errors = new LinkedHashSet<>();
        if (format != null) {
            if(format.getName().equals("ipv6")) {
                if(!node.textValue().trim().equals(node.textValue())) {
                    if (assertionsEnabled) {
                        // leading and trailing spaces
                        errors.add(message().instanceNode(node).instanceLocation(instanceLocation)
                                .locale(executionContext.getExecutionConfig().getLocale())
                                .failFast(executionContext.isFailFast())
                                .arguments(format.getName(), format.getErrorMessageDescription()).build());
                    }
                } else if(node.textValue().contains("%")) {
                    if (assertionsEnabled) {
                        // zone id is not part of the ipv6
                        errors.add(message().instanceNode(node).instanceLocation(instanceLocation)
                                .locale(executionContext.getExecutionConfig().getLocale())
                                .arguments(format.getName(), format.getErrorMessageDescription()).build());
                    }
                }
            }
            try {
                errors.addAll(format.validate(executionContext, validationContext, node, rootNode, instanceLocation,
                        assertionsEnabled, () -> this.message(), this));
            } catch (PatternSyntaxException pse) {
                // String is considered valid if pattern is invalid
                logger.error("Failed to apply pattern on {}: Invalid RE syntax [{}]", instanceLocation, format.getName(), pse);
            }
        }

        return Collections.unmodifiableSet(errors);
    }

}
