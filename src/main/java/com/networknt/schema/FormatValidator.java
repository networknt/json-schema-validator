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
import java.util.Set;
import java.util.regex.PatternSyntaxException;

public class FormatValidator extends BaseFormatJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(FormatValidator.class);

    private final Format format;
    
    public FormatValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext, Format format,
            ErrorMessageType errorMessageType, Keyword keyword) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, errorMessageType, keyword, validationContext);
        this.format = format;
    }

    public FormatValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext, Format format, ValidatorTypeCode type) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, type, type, validationContext);
        this.format = format;
    }

    /**
     * Gets the annotation value.
     * 
     * @return the annotation value
     */
    protected Object getAnnotationValue() {
        if (this.format != null) {
            return this.format.getName();
        }
        return this.schemaNode.isTextual() ? schemaNode.textValue() : null;
    }
    
    protected boolean isStrict(ExecutionContext executionContext, ValidationContext validationContext) {
        return validationContext.getConfig().isStrict(getKeyword());
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);

        /*
         * Annotations must be collected even if the format is unknown according to the specification.
         */
        if (collectAnnotations(executionContext)) {
            Object annotationValue = getAnnotationValue();
            if (annotationValue != null) {
                putAnnotation(executionContext,
                        annotation -> annotation.instanceLocation(instanceLocation).value(annotationValue));
            }
        }

        boolean assertionsEnabled = isAssertionsEnabled(executionContext);
        if (this.format != null) {
            try {
                return format.validate(executionContext, validationContext, node, rootNode, instanceLocation,
                        assertionsEnabled, () -> this.message(), this);
            } catch (PatternSyntaxException pse) {
                // String is considered valid if pattern is invalid
                logger.error("Failed to apply pattern on {}: Invalid RE syntax [{}]", instanceLocation,
                        format.getName(), pse);
            }
        } else {
            /*
             * Unknown formats should create an assertion according to the specification.
             */
//            if (assertionsEnabled && isStrict(executionContext, validationContext) && this.schemaNode.isTextual()) {
//                return Collections.singleton(message().instanceLocation(instanceLocation).instanceNode(node)
//                        .messageKey("format.unknown").arguments(schemaNode.textValue()).build());
//            }
        }
        return Collections.emptySet();
    }
}
